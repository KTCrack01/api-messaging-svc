package com.kt.api_messaging_svc.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_recipients")
@Getter
public class MessageRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_recipients_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Messages messages;

    @Column(name = "phone_num", length = 20, nullable = false)
    private String phoneNum;

    @Column(name = "send_at")
    private LocalDateTime sendAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "status", length = 255)
    private String status;

    @Column(name = "provider_sid")
    private String providerSid;

    @Column(name = "last_error")
    private String lastError;

    protected MessageRecipient(){
    }

    public void setProviderSid(String providerSid) {
        this.providerSid = providerSid;
    }

    public void markDelivered(LocalDateTime ts) {
        this.status = "delivered";
        this.deliveredAt = ts;
    }

    public void markFailed(String err) {
        this.status = "failed";
        this.lastError = err;
    }

    @Builder
    public MessageRecipient(Messages messages, String phoneNum, LocalDateTime sendAt, LocalDateTime deliveredAt, String status, String providerSid, String lastError) {
        this.messages = messages;
        this.phoneNum = phoneNum;
        this.sendAt = sendAt;
        this.deliveredAt = deliveredAt;
        this.status = status;
        this.providerSid = providerSid;
        this.lastError = lastError;
    }
}
