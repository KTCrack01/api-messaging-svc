package com.kt.api_messaging_svc.dto;

import com.kt.api_messaging_svc.entity.Messages;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "메시지 응답")
public class MessageResponse {

    @Schema(description = "메시지 ID", example = "1")
    private Long id;
    
    @Schema(description = "메시지 내용", example = "안녕하세요, 테스트 메시지입니다.")
    private String body;
    
    @Schema(description = "생성 시간", example = "2024-01-01T10:30:00")
    private LocalDateTime createdAt;

    public static MessageResponse from(Messages m) {
        return new MessageResponse(
                m.getId(),
                m.getBody(),
                m.getCreatedAt()
        );
    }
}
