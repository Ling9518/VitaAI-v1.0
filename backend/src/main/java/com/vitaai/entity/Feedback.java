package com.vitaai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('BUG','FEATURE','COMPLAINT','COMPLIMENT','OTHER')")
    private FeedbackType type;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 100)
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('SUBMITTED','IN_PROGRESS','RESOLVED','CLOSED') DEFAULT 'SUBMITTED'")
    @Builder.Default
    private FeedbackStatus status = FeedbackStatus.SUBMITTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handler_id")
    private User handler;

    @Column(columnDefinition = "TEXT")
    private String handlerNotes;

    @Column(columnDefinition = "TEXT")
    private String replyContent;

    private LocalDateTime repliedAt;

    private Integer rating;

    @Column(length = 50)
    private String ipAddress;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum FeedbackType { BUG, FEATURE, COMPLAINT, COMPLIMENT, OTHER }
    public enum FeedbackStatus { SUBMITTED, IN_PROGRESS, RESOLVED, CLOSED }
}
