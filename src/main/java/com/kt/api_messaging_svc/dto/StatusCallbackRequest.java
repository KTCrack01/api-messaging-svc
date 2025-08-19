package com.kt.api_messaging_svc.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusCallbackRequest {
    private String MessageSid;
    private String MessageStatus;
    private String ErrorCode;
    private String ErrorMessage;
}
