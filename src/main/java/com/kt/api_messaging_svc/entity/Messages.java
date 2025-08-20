package com.kt.api_messaging_svc.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
@Getter
public class Messages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @Column(name = "user_email", length = 255, nullable = false)
    private String userEmail;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "sender_phone", length = 20, nullable = false)
    private String senderPhone;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @OneToMany(mappedBy = "messages")
    private List<MessageRecipient> recipients = new ArrayList<>();

    protected Messages(){
    }

    @Builder
    public Messages(String userEmail, String body, String senderPhone, LocalDateTime createdAt, LocalDateTime scheduledAt) {
        this.userEmail = userEmail;
        this.body = body;
        this.senderPhone = senderPhone;
        this.createdAt = createdAt;
        this.scheduledAt = scheduledAt;
    }
}
