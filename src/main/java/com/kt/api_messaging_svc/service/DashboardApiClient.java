package com.kt.api_messaging_svc.service;

import com.kt.api_messaging_svc.dto.MessageDashboardDataCreateRequest;
import com.kt.api_messaging_svc.dto.StatusUpdateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DashboardApiClient {
    private final RestTemplate restTemplate;

    @Value("${message-dashboard.url}")
    private String baseUrl; // API 주소

    public DashboardApiClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public void sendDashboardData(MessageDashboardDataCreateRequest req) {
        try {
            restTemplate.postForEntity(
                    baseUrl + "/api/v1/dashboard/data", // 실제 엔드포인트
                    req,
                    Void.class
            );
        } catch (Exception e) {
            System.err.println("Dashboard API 호출 실패: " + e.getMessage());
        }
    }

    public void sendDashboardData(StatusUpdateRequest req) {
        try {
            restTemplate.postForEntity(
                    baseUrl + "/api/v1/dashboard/data/status", // 실제 엔드포인트
                    req,
                    Void.class
            );
        } catch (Exception e) {
            System.err.println("Status Update API 호출 실패: " + e.getMessage());
        }
    }
}
