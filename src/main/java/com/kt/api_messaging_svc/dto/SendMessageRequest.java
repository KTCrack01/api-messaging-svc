package com.kt.api_messaging_svc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메시지 전송 요청")
public class SendMessageRequest {
    @Schema(description = "발신자 이메일", example = "sender@kt.com", required = true)
    private String userEmail;
    
    @Schema(description = "메시지 내용", example = "안녕하세요, 테스트 메시지입니다.", required = true)
    private String body;
    
    @Schema(description = "수신자 전화번호 목록", example = "[\"01012345678\", \"01087654321\"]", required = true)
    private List<String> recipients;
}

