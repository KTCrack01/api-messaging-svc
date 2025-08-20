package com.kt.api_messaging_svc.dto;

import lombok.Getter;

@Getter
public class StatusUpdateRequest {
    private String status;
    private String providerSid;

    public StatusUpdateRequest(String status, String providerSid) {
        this.status = status;
        this.providerSid = providerSid;
    }
}
