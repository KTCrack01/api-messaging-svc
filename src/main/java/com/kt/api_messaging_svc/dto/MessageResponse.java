package com.kt.api_messaging_svc.dto;


import com.kt.api_messaging_svc.entity.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private String body;
    private LocalDateTime createdAt;

    public static MessageResponse from(Messages m) {
        return new MessageResponse(
                m.getId(),
                m.getBody(),
                m.getCreatedAt()
        );
    }
}
