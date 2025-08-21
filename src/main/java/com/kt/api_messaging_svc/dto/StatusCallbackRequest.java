package com.kt.api_messaging_svc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Twilio 메시지 상태 콜백 요청")
public class StatusCallbackRequest {
    @Schema(description = "Twilio 메시지 SID", example = "SM1234567890abcdef1234567890abcdef")
    private String MessageSid;
    
    @Schema(description = "메시지 상태", example = "delivered", allowableValues = {"queued", "sent", "received", "delivered", "undelivered", "failed"})
    private String MessageStatus;
    
    @Schema(description = "에러 코드", example = "30008")
    private String ErrorCode;
    
    @Schema(description = "에러 메시지", example = "Unknown error")
    private String ErrorMessage;
}
