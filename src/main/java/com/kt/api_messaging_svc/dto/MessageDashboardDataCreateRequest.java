package com.kt.api_messaging_svc.dto;

import lombok.Getter;

@Getter
public class MessageDashboardDataCreateRequest {
    private String userEmail;
    private String phoneNum;
    private String sendAt;
    private String status;
    private String providerSid;

    public MessageDashboardDataCreateRequest(String userEmail, String phoneNum, String sendAt, String status, String providerSid) {
        this.userEmail = userEmail;
        this.phoneNum = phoneNum;
        this.sendAt = sendAt;
        this.status = status;
        this.providerSid = providerSid;
    }
}
