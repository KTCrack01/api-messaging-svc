package com.kt.api_messaging_svc.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    private String userEmail;
    private String body;
    private String from;
    private List<String> recipients;
}

