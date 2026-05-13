package com.vitaai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitaai.ai.DeepSeekClient;
import com.vitaai.ai.DeepSeekClient.ChatMessage;
import com.vitaai.ai.MedicalSkillService;
import com.vitaai.dto.ChatRequest;
import com.vitaai.entity.*;
import com.vitaai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final DeepSeekClient deepSeekClient;
    private final MedicalSkillService medicalSkillService;
    private final DiagnosisRecordRepository diagnosisRecordRepository;
    private final AiConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> chat(Long userId, ChatRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 获取或创建诊断记录
        DiagnosisRecord record;
        boolean isNew = request.getConversationId() == null || request.getConversationId().isEmpty();
        if (isNew) {
            record = DiagnosisRecord.builder()
                    .user(user)
                    .conversationId(UUID.randomUUID().toString())
                    .feedbackAccuracy(DiagnosisRecord.FeedbackAccuracy.PENDING)
                    .build();
            if (request.getHealthRecordId() != null) {
                healthRecordRepository.findById(request.getHealthRecordId())
                        .ifPresent(record::setHealthRecord);
            }
            record = diagnosisRecordRepository.save(record);
        } else {
            record = diagnosisRecordRepository.findByConversationId(request.getConversationId())
                    .orElseThrow(() -> new RuntimeException("对话记录不存在"));
        }

        // 保存用户消息
        AiConversation userMsg = AiConversation.builder()
                .diagnosisRecord(record)
                .user(user)
                .senderType(AiConversation.SenderType.USER)
                .content(request.getMessage())
                .build();
        conversationRepository.save(userMsg);

        // 构建AI对话历史
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", getSystemPrompt(userId)));

        List<AiConversation> history = conversationRepository
                .findByDiagnosisRecordIdOrderByCreatedAtAsc(record.getId());
        for (AiConversation conv : history) {
            String role = conv.getSenderType() == AiConversation.SenderType.USER ? "user" : "assistant";
            messages.add(new ChatMessage(role, conv.getContent() != null ? conv.getContent() : ""));
        }

        // 调用AI
        long startTime = System.currentTimeMillis();
        String aiResponse = deepSeekClient.chat(messages);
        int latency = (int) (System.currentTimeMillis() - startTime);

        // 保存AI回复
        AiConversation aiMsg = AiConversation.builder()
                .diagnosisRecord(record)
                .user(user)
                .senderType(AiConversation.SenderType.AI)
                .content(aiResponse)
                .aiModelUsed("astron-code-latest")
                .latencyMs(latency)
                .build();
        conversationRepository.save(aiMsg);

        // 更新诊断记录
        record.setMessageCount(record.getMessageCount() + 2);
        record.setSymptomSummary(extractSummary(request.getMessage()));
        record.setUpdatedAt(LocalDateTime.now());
        diagnosisRecordRepository.save(record);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("conversationId", record.getConversationId());
        result.put("messageId", aiMsg.getId());
        result.put("senderType", "AI");
        result.put("content", aiResponse);
        result.put("aiModel", "astron-code-latest");
        result.put("tokensUsed", aiMsg.getTokensUsed());
        result.put("createdAt", aiMsg.getCreatedAt().toString());
        result.put("isNewConversation", isNew);

        return result;
    }

    public Page<DiagnosisRecord> getHistory(Long userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        return diagnosisRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Map<String, Object> getDiagnosisDetail(Long id) {
        DiagnosisRecord record = diagnosisRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("诊断记录不存在"));
        List<AiConversation> messages = conversationRepository
                .findByDiagnosisRecordIdOrderByCreatedAtAsc(id);

        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("conversationId", record.getConversationId());
        result.put("symptomSummary", record.getSymptomSummary());
        result.put("severityLevel", record.getSeverityLevel());
        result.put("advice", record.getAdvice());
        result.put("needsHospital", record.getNeedsHospital());
        result.put("feedbackAccuracy", record.getFeedbackAccuracy());
        result.put("messageCount", record.getMessageCount());
        result.put("createdAt", record.getCreatedAt());
        result.put("messages", messages);
        return result;
    }

    @Transactional
    public void submitFeedback(Long diagnosisId, String accuracy, String comments) {
        DiagnosisRecord record = diagnosisRecordRepository.findById(diagnosisId)
                .orElseThrow(() -> new RuntimeException("诊断记录不存在"));
        if (record.getFeedbackAccuracy() != DiagnosisRecord.FeedbackAccuracy.PENDING) {
            throw new RuntimeException("已提交过反馈");
        }
        record.setFeedbackAccuracy(DiagnosisRecord.FeedbackAccuracy.valueOf(accuracy));
        record.setFeedbackDetail(comments);
        record.setFeedbackAt(LocalDateTime.now());
        diagnosisRecordRepository.save(record);
    }

    private String getSystemPrompt(Long userId) {
        StringBuilder sb = new StringBuilder(medicalSkillService.getSystemPrompt());
        // 附加用户健康档案信息
        healthRecordRepository.findByUserId(userId).ifPresent(hr -> {
            sb.append("\n\n【当前用户健康档案】\n");
            if (hr.getMedicalHistory() != null) sb.append("病史：").append(hr.getMedicalHistory()).append("\n");
            if (hr.getAllergyHistory() != null) sb.append("过敏史：").append(hr.getAllergyHistory()).append("\n");
            if (hr.getMedicationRecords() != null) sb.append("用药记录：").append(hr.getMedicationRecords()).append("\n");
        });
        return sb.toString();
    }

    private String extractSummary(String message) {
        if (message == null) return "";
        return message.length() > 200 ? message.substring(0, 200) + "..." : message;
    }
}
