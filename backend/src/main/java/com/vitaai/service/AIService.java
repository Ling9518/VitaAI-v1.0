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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    @Value("${ai.model}")
    private String aiModel;

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
                .aiModelUsed(aiModel)
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
        result.put("aiModel", aiModel);
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

    public SseEmitter streamChat(Long userId, ChatRequest request) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 min timeout
        Thread sseThread = new Thread(() -> {
            try {
                // Send initial connected event immediately
                Map<String, Object> connected = new HashMap<>();
                connected.put("type", "connected");
                emitter.send(SseEmitter.event().name("message").data(connected));

                Map<String, Object> result = chat(userId, request);
                String content = (String) result.get("content");
                String conversationId = (String) result.get("conversationId");

                // Stream content in natural word/codepoint boundaries
                int i = 0;
                int len = content.length();
                while (i < len) {
                    int end = Math.min(i + randomChunkSize(), len);
                    if (end < len && Character.isHighSurrogate(content.charAt(end - 1))) {
                        end++;
                    }
                    String chunk = content.substring(i, end);
                    Map<String, Object> event = new HashMap<>();
                    event.put("type", "chunk");
                    event.put("content", chunk);
                    event.put("conversationId", conversationId);
                    emitter.send(SseEmitter.event().name("message").data(event));
                    i = end;
                    Thread.sleep(15 + (int)(Math.random() * 25)); // 15-40ms typing feel
                }

                Map<String, Object> done = new HashMap<>();
                done.put("type", "done");
                done.put("conversationId", conversationId);
                emitter.send(SseEmitter.event().name("message").data(done));
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE stream error", e);
                emitter.completeWithError(e);
            }
        });
        sseThread.start();
        return emitter;
    }

    private int randomChunkSize() {
        return 8 + (int)(Math.random() * 28); // 8-35 chars per chunk
    }

    public Map<String, Object> generateReport(Long diagnosisId) {
        DiagnosisRecord record = diagnosisRecordRepository.findById(diagnosisId)
                .orElseThrow(() -> new RuntimeException("诊断记录不存在"));
        List<AiConversation> messages = conversationRepository
                .findByDiagnosisRecordIdOrderByCreatedAtAsc(diagnosisId);

        StringBuilder report = new StringBuilder();
        report.append("【VitaAI 智能诊断报告】\n\n");
        report.append("报告时间：").append(LocalDateTime.now()).append("\n");
        report.append("症状摘要：").append(record.getSymptomSummary() != null ? record.getSymptomSummary() : "无").append("\n");
        report.append("严重程度：").append(record.getSeverityLevel() != null ? record.getSeverityLevel().name() : "未评估").append("\n");
        report.append("是否需要就医：").append(Boolean.TRUE.equals(record.getNeedsHospital()) ? "是" : "否").append("\n");
        report.append("总消息数：").append(messages.size()).append("\n\n");
        report.append("【对话记录】\n");
        for (AiConversation msg : messages) {
            String role = msg.getSenderType() == AiConversation.SenderType.USER ? "用户" : "AI医生";
            report.append(role).append(": ").append(msg.getContent()).append("\n\n");
        }
        report.append("\n【免责声明】\n内容为AI诊断，想要更准确诊断，请去正规医院就诊。");

        Map<String, Object> result = new HashMap<>();
        result.put("diagnosisId", diagnosisId);
        result.put("report", report.toString());
        result.put("generatedAt", LocalDateTime.now().toString());
        return result;
    }

    public List<Map<String, Object>> getSkills() {
        return medicalSkillService.getActiveSkills().stream().map(skill -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", skill.getId());
            m.put("name", skill.getName());
            m.put("description", skill.getDescription());
            return m;
        }).toList();
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

    @Transactional
    public void deleteDiagnosis(Long diagnosisId) {
        DiagnosisRecord record = diagnosisRecordRepository.findById(diagnosisId)
                .orElseThrow(() -> new RuntimeException("诊断记录不存在"));
        conversationRepository.deleteByDiagnosisRecordId(diagnosisId);
        diagnosisRecordRepository.delete(record);
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
