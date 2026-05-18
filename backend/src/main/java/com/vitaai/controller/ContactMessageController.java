package com.vitaai.controller;

import com.vitaai.dto.ApiResponse;
import com.vitaai.entity.ContactMessage;
import com.vitaai.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact-messages")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageRepository contactMessageRepository;

    @PostMapping
    public ApiResponse<Map<String, String>> submit(@RequestBody Map<String, String> body) {
        ContactMessage msg = ContactMessage.builder()
                .name(body.get("name"))
                .phone(body.get("phone"))
                .email(body.get("email"))
                .content(body.get("content"))
                .build();
        contactMessageRepository.save(msg);
        return ApiResponse.success(Map.of("message", "留言已提交"));
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        var p = contactMessageRepository.findAll(
                PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.success(Map.of(
                "list", p.getContent(),
                "pagination", Map.of("total", p.getTotalElements(), "page", page, "pageSize", pageSize)
        ));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Map<String, String>> markRead(@PathVariable Long id) {
        ContactMessage msg = contactMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在"));
        msg.setIsRead(true);
        contactMessageRepository.save(msg);
        return ApiResponse.success(Map.of("message", "已标记为已读"));
    }
}
