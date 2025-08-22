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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 2) 수신자별 전송 + 저장 (개별 실패는 전체 롤백 유발하지 않도록 try/catch)
        for (String to : recipients) {
            LocalDateTime now = LocalDateTime.now();
            String formatted = now.format(formatter);

            // 기존/최초 수신자 행 upsert 느낌으로 조회 (중복 전송 방지)
            MessageRecipient recipient = messageRecipientRepository
                    .findByMessagesIdAndPhoneNum(messagesEntity.getId(), to)
                    .orElseGet(() -> MessageRecipient.builder()
                            .messages(messagesEntity)
                            .phoneNum(to)
                            .sendAt(now)
                            .deliveredAt(null)
                            .status(null)
                            .providerSid(null)
                            .lastError(null)
                            .attemptCount(0)
                            .build());

            try {
                sendWithRetryAndPersist(recipient, body, userEmail, formatted);
            } catch (Exception finalEx) {
                // 모든 재시도 실패 시
                recipient.setStatus("failed");
                recipient.setLastError(finalEx.getMessage());
                messageRecipientRepository.save(recipient);

                MessageDashboardDataCreateRequest req =
                        new MessageDashboardDataCreateRequest(userEmail, to, formatted, "failed", null);
                dashboardApiClient.sendDashboardData(req);

                System.out.println("SMS final failed to " + to + ": " + finalEx.getMessage());
            }
        }

        return messagesEntity;
    }

    /**
     * 재시도 로직 포함 전송 함수
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendWithRetryAndPersist(MessageRecipient recipient,
                                        String body,
                                        String userEmail,
                                        String formattedTimestamp) throws InterruptedException {

        final int maxAttempts = 3; // 총 3번
        int attempt = 0;

        while (attempt < maxAttempts) {
            attempt++;
            recipient.setAttemptCount(attempt);
            messageRecipientRepository.save(recipient);

            try {
                // 이미 성공한 건이면 재시도 불가(중복 방지)
                if (recipient.getProviderSid() != null) {
                    return;
                }

                // Twilio 전송 시도
                com.twilio.rest.api.v2010.account.Message twilioMsg =
                        com.twilio.rest.api.v2010.account.Message.creator(
                                        new com.twilio.type.PhoneNumber(recipient.getPhoneNum()),
                                        new com.twilio.type.PhoneNumber(from),
                                        body
                                )
                                .setStatusCallback(URI.create(baseUrl + "/api/v1/messages/status"))
                                .create();

                // 성공 처리
                recipient.setProviderSid(twilioMsg.getSid());
                recipient.setStatus("sent"); // 웹훅에서 delivered/failed로 최종 갱신
                recipient.setLastError(null);
                messageRecipientRepository.save(recipient);

                // 대시보드 알림(시도 번호 포함: 선택)
                MessageDashboardDataCreateRequest req =
                        new MessageDashboardDataCreateRequest(
                                userEmail, recipient.getPhoneNum(), formattedTimestamp, "sent", twilioMsg.getSid());
                dashboardApiClient.sendDashboardData(req);

                System.out.println("Sent to " + recipient.getPhoneNum() + " | sid=" + twilioMsg.getSid()
                        + " | attempt=" + attempt);
                return;

            } catch (com.twilio.exception.ApiException e) {
                // 재시도 여부 판단
                if (!isRetryableTwilioException(e)) {
                    // 영구 실패 → 즉시 종료
                    throw e;
                }

                // 재시도 예정 → 상태/대시보드에 중간 기록 남겨도 됨
                log.warn("⚠️ [Retry Scheduled] to={} | attempt={} | error={}",
                        recipient.getPhoneNum(), attempt, e.getMessage());


                MessageDashboardDataCreateRequest req =
                        new MessageDashboardDataCreateRequest(
                                userEmail, recipient.getPhoneNum(), formattedTimestamp, "retrying", null);
                dashboardApiClient.sendDashboardData(req);

                // 지수 백오프 + 지터
                long baseMillis = (long) Math.pow(3, attempt - 1) * 1000L; // 1s, 3s, 9s
                long jitter = Math.round(baseMillis * (Math.random() * 0.4 - 0.2)); // ±20%
                Thread.sleep(Math.max(500, baseMillis + jitter));

                if (attempt >= maxAttempts) {
                    // 루프 종료 → while 밖에서 Exception 던지도록
                    throw e;
                }
            } catch (java.net.SocketTimeoutException | java.net.ConnectException ex) {
                // 네트워크 계열도 재시도
                if (attempt >= maxAttempts) throw new RuntimeException(ex);
                long baseMillis = (long) Math.pow(3, attempt - 1) * 1000L;
                long jitter = Math.round(baseMillis * (Math.random() * 0.4 - 0.2));
                Thread.sleep(Math.max(500, baseMillis + jitter));
            }
        }
    }

    /**
     * 재시도 대상 판단 로직
     */
    private boolean isRetryableTwilioException(com.twilio.exception.ApiException e) {
        Integer status = e.getStatusCode(); // Twilio ApiException은 HTTP status 포함
        if (status == null) return true; // 방어적: 알 수 없으면 재시도 1~2회 허용

        // 5xx 서버 에러, 429 레이트 리밋 → 재시도
        if (status >= 500 || status == 429) return true;

        // 4xx는 대부분 영구 오류 → 재시도 금지
        // 예: 21610(수신거부), 21614(잘못된 번호), 21608(발신번호 SMS 불가) 등
        return false;
    }

    public List<MessageResponse> getMessagesByUserEmail(String userEmail) {
        return messageRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(MessageResponse::from)
                .toList();
    }
}
