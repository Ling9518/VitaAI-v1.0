package com.vitaai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    private String conversationId;

    @NotBlank(message = "消息不能为空")
    private String message;

    private Long healthRecordId;

    private Long symptomAssessmentId;

    private boolean stream;
}
