package com.vitaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_record_id")
    private DiagnosisRecord diagnosisRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('USER','AI','SYSTEM')")
    private SenderType senderType;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('TEXT','IMAGE','AUDIO','FILE') DEFAULT 'TEXT'")
    private MessageType messageType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String skillsTriggered;

    @Column(length = 50)
    private String aiModelUsed;

    @Builder.Default
    private Integer tokensUsed = 0;

    @Builder.Default
    private Integer latencyMs = 0;

    @Builder.Default
    private Boolean isMedicalQuestion = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum SenderType {
        USER, AI, SYSTEM
    }

    public enum MessageType {
        TEXT, IMAGE, AUDIO, FILE
    }
}
