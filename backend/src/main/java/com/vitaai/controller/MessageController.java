package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.entity.Message;
import com.vitaai.entity.User;
import com.vitaai.repository.MessageRepository;
import com.vitaai.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @PostMapping
    public ApiResponse<Map<String, String>> create(@RequestBody Map<String, String> body,
                                                   HttpServletRequest request) {
        User user = getCurrentUser(request);
        Message msg = Message.builder()
                .user(user)
                .content(body.get("content"))
                .build();
        messageRepository.save(msg);
        return ApiResponse.success("留言已提交，医生将尽快回复", Map.of("message", "留言已提交"));
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        User user = getCurrentUser(request);
        boolean isAdminOrDoctor = user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.DOCTOR;

        if (isAdminOrDoctor && status != null) {
            Message.Status s = Message.Status.valueOf(status.toUpperCase());
            var p = messageRepository.findByStatusOrderByCreatedAtDesc(s,
                    PageRequest.of(page - 1, pageSize));
            return ApiResponse.success(buildPageData(p, page, pageSize));
        }

        if (isAdminOrDoctor) {
            var p = messageRepository.findAll(
                    PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
            return ApiResponse.success(buildPageData(p, page, pageSize));
        }

        var p = messageRepository.findByUserIdOrderByCreatedAtDesc(user.getId(),
                PageRequest.of(page - 1, pageSize));
        return ApiResponse.success(buildPageData(p, page, pageSize));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, String>> update(@PathVariable Long id,
                                                   @RequestBody Map<String, String> body,
                                                   HttpServletRequest request) {
        User user = getCurrentUser(request);
        Message msg = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在"));

        // 用户只能编辑自己的留言，管理员可以编辑任何留言
        boolean isOwner = msg.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("只能编辑自己的留言");
        }

        msg.setContent(body.get("content"));
        msg.setUpdatedAt(LocalDateTime.now());
        messageRepository.save(msg);
        return ApiResponse.success(Map.of("message", "留言已更新"));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, String>> delete(@PathVariable Long id,
                                                   HttpServletRequest request) {
        User user = getCurrentUser(request);
        Message msg = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在"));

        // 用户只能删除自己的留言，管理员可以删除任何留言
        boolean isOwner = msg.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("只能删除自己的留言");
        }

        messageRepository.delete(msg);
        return ApiResponse.success(Map.of("message", "留言已删除"));
    }

    @PutMapping("/{id}/reply")
    public ApiResponse<Map<String, String>> reply(@PathVariable Long id,
                                                  @RequestBody Map<String, String> body,
                                                  HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.DOCTOR)
            throw new RuntimeException("无权限");

        Message msg = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在"));

        // 如果已有回复，医生只能编辑自己的回复，管理员可以编辑任何回复
        if (msg.getReply() != null && user.getRole() == User.Role.DOCTOR) {
            if (msg.getRepliedBy() == null || !msg.getRepliedBy().getId().equals(user.getId())) {
                throw new RuntimeException("只能编辑自己的回复");
            }
        }

        boolean isFirstReply = msg.getReply() == null;
        msg.setReply(body.get("reply"));
        msg.setRepliedBy(user);
        if (isFirstReply) {
            msg.setRepliedAt(LocalDateTime.now());
        }
        messageRepository.save(msg);
        return ApiResponse.success(Map.of("message", isFirstReply ? "已回复" : "回复已更新"));
    }

    @DeleteMapping("/{id}/reply")
    public ApiResponse<Map<String, String>> withdrawReply(@PathVariable Long id,
                                                           HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.DOCTOR)
            throw new RuntimeException("无权限");

        Message msg = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在"));

        // 医生只能撤回自己的回复，管理员可以撤回任何回复
        if (user.getRole() == User.Role.DOCTOR) {
            if (msg.getRepliedBy() == null || !msg.getRepliedBy().getId().equals(user.getId())) {
                throw new RuntimeException("只能撤回自己的回复");
            }
        }

        msg.setReply(null);
        msg.setRepliedBy(null);
        msg.setRepliedAt(null);
        msg.setStatus(Message.Status.UNRESOLVED);
        messageRepository.save(msg);
        return ApiResponse.success(Map.of("message", "回复已撤回"));
    }

    @PutMapping("/admin/{id}")
    public ApiResponse<Map<String, String>> adminEdit(@PathVariable Long id,
                                                       @RequestBody Map<String, String> body,
                                                       HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user.getRole() != User.Role.ADMIN)
            throw new RuntimeException("无权限");
        Message msg = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在"));
        if (body.containsKey("content")) {
            msg.setContent(body.get("content"));
            msg.setUpdatedAt(LocalDateTime.now());
        }
        if (body.containsKey("reply")) {
            boolean isFirstReply = msg.getReply() == null;
            msg.setReply(body.get("reply"));
            msg.setRepliedBy(user);
            if (isFirstReply) {
                msg.setRepliedAt(LocalDateTime.now());
            }
        }
        messageRepository.save(msg);
        return ApiResponse.success(Map.of("message", "已更新"));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Map<String, String>> toggleStatus(@PathVariable Long id,
                                                         HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.DOCTOR)
            throw new RuntimeException("无权限");

        Message msg = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在"));

        // 医生只能切换自己回复的留言状态，管理员可以切换任何留言状态
        if (user.getRole() == User.Role.DOCTOR) {
            if (msg.getRepliedBy() == null || !msg.getRepliedBy().getId().equals(user.getId())) {
                throw new RuntimeException("只能变更自己回复过的留言状态");
            }
        }

        msg.setStatus(msg.getStatus() == Message.Status.UNRESOLVED ? Message.Status.RESOLVED : Message.Status.UNRESOLVED);
        messageRepository.save(msg);
        return ApiResponse.success(Map.of("message", "状态已更新"));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> stats() {
        long unresolved = messageRepository.countByStatus(Message.Status.UNRESOLVED);
        long resolved = messageRepository.countByStatus(Message.Status.RESOLVED);
        return ApiResponse.success(Map.of("unresolved", unresolved, "resolved", resolved));
    }

    private Map<String, Object> buildPageData(
            org.springframework.data.domain.Page<Message> p, int page, int pageSize) {
        List<Map<String, Object>> list = p.getContent().stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("content", m.getContent());
            map.put("reply", m.getReply());
            map.put("status", m.getStatus().name());
            map.put("createdAt", m.getCreatedAt());
            map.put("updatedAt", m.getUpdatedAt());
            map.put("repliedAt", m.getRepliedAt());
            Map<String, Object> userInfo = new LinkedHashMap<>();
            userInfo.put("id", m.getUser().getId());
            userInfo.put("username", m.getUser().getUsername());
            map.put("user", userInfo);
            if (m.getRepliedBy() != null) {
                Map<String, Object> replierInfo = new LinkedHashMap<>();
                replierInfo.put("id", m.getRepliedBy().getId());
                replierInfo.put("username", m.getRepliedBy().getUsername());
                map.put("repliedBy", replierInfo);
            }
            return map;
        }).toList();
        return Map.of(
                "list", list,
                "pagination", Map.of("total", p.getTotalElements(), "page", page, "pageSize", pageSize)
        );
    }
}
