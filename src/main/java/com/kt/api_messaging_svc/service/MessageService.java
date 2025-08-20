package com.kt.api_messaging_svc.service;

import com.kt.api_messaging_svc.dto.MessageDashboardDataCreateRequest;
import com.kt.api_messaging_svc.dto.MessageResponse;
import com.kt.api_messaging_svc.entity.MessageRecipient;
import com.kt.api_messaging_svc.entity.Messages;
import com.kt.api_messaging_svc.repository.MessageRecipientRepository;
import com.kt.api_messaging_svc.repository.MessageRepository;
import com.twilio.Twilio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final MessageRecipientRepository messageRecipientRepository;
    private final DashboardApiClient dashboardApiClient;

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${message.url}")
    private String baseUrl; // API 주소

    private String from = "+15177656650";

    @Transactional
    public Messages sendManyMessage(String userEmail, String body, List<String> recipients) {
        Twilio.init(accountSid, authToken);

        // 1) 부모 메시지 저장
        Messages messagesEntity = Messages.builder()
                .userEmail(userEmail)
                .body(body)
                .senderPhone(from)
                .createdAt(LocalDateTime.now())
                .scheduledAt(null)
                .build();

        messageRepository.save(messagesEntity);

        // 2) 수신자별 전송 + 저장
        for (String to : recipients) {
            LocalDateTime now = LocalDateTime.now();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String formatted = now.format(formatter);

            try {
                // Twilio 전송
                com.twilio.rest.api.v2010.account.Message twilioMsg =
                        com.twilio.rest.api.v2010.account.Message.creator(
                                        new com.twilio.type.PhoneNumber(to),
                                        new com.twilio.type.PhoneNumber(from),
                                        body
                                )
                                .setStatusCallback(URI.create(baseUrl+"/api/v1/messages/status"))
                                .create();

                // 성공 → provider_sid만 세팅(나머지는 null 유지 가능)
                MessageRecipient recipient = MessageRecipient.builder()
                        .messages(messagesEntity)
                        .phoneNum(to)
                        .sendAt(now)
                        .deliveredAt(null)     // 웹훅에서 업데이트 예정
                        .status(null)          // 정책에 따라 "sent"로 두고 싶으면 "sent"로
                        .providerSid(twilioMsg.getSid())
                        .lastError(null)
                        .build();

                messageRecipientRepository.save(recipient);
                System.out.println("Sent to " + to + " | sid=" + twilioMsg.getSid());


                MessageDashboardDataCreateRequest req =
                        new MessageDashboardDataCreateRequest(userEmail, to, formatted, "delivered", twilioMsg.getSid());
                dashboardApiClient.sendDashboardData(req);

            } catch (com.twilio.exception.ApiException e) {
                // 실패 → 요구사항대로 기록
                MessageRecipient failed = MessageRecipient.builder()
                        .messages(messagesEntity)
                        .phoneNum(to)
                        .sendAt(now)
                        .deliveredAt(null)
                        .status("failed")
                        .providerSid(null)
                        .lastError(e.getMessage())   // 요구가 '다른 값은 null' 이므로 null
                        .build();

                messageRecipientRepository.save(failed);

                MessageDashboardDataCreateRequest req =
                        new MessageDashboardDataCreateRequest(userEmail, to, formatted, "failed", null);
                dashboardApiClient.sendDashboardData(req);

                // 로그만 남기고 다음 번호 계속 처리
                System.out.println("SMS send failed to "+to+": "+e.getMessage()+" (code="+e.getCode()+")");
            }
        }

        return messagesEntity;
    }

    public List<MessageResponse> getMessagesByUserEmail(String userEmail) {
        return messageRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(MessageResponse::from)
                .toList();
    }
}
