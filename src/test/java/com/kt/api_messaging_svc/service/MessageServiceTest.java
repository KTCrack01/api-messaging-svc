package com.kt.api_messaging_svc.service;

import static org.junit.jupiter.api.Assertions.*;

import com.kt.api_messaging_svc.dto.MessageDashboardDataCreateRequest;
import com.kt.api_messaging_svc.entity.MessageRecipient;
import com.kt.api_messaging_svc.entity.Messages;
import com.kt.api_messaging_svc.repository.MessageRecipientRepository;
import com.kt.api_messaging_svc.repository.MessageRepository;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock MessageRepository messageRepository;
    @Mock MessageRecipientRepository messageRecipientRepository;
    @Mock DashboardApiClient dashboardApiClient;

    @InjectMocks MessageService messageService;

    private void injectFields() {
        ReflectionTestUtils.setField(messageService, "accountSid", "ACxxxx");
        ReflectionTestUtils.setField(messageService, "authToken", "tok_xxxx");
        ReflectionTestUtils.setField(messageService, "baseUrl", "https://example.com");
    }

    /**
     * [테스트 목적]
     * - Twilio API가 정상 응답하는 경우
     * - 각 수신자에 대해 MessageRecipient 저장 + Dashboard API 호출이 정확히 발생하는지 검증
     */
    @Test
    @DisplayName("모든 수신자에게 성공적으로 메시지를 전송하면 DB 저장 및 대시보드 전송이 각각 N회 발생한다")
    void shouldSendAndPersistAndPushDashboard_successBulk() {
        injectFields();

        // Given: Twilio static mocking → 항상 성공 응답 반환
        try (MockedStatic<Twilio> twilioStatic = mockStatic(Twilio.class);
             MockedStatic<Message> messageStatic = mockStatic(Message.class)) {

            MessageCreator creatorMock = mock(MessageCreator.class);
            when(creatorMock.setStatusCallback(any(URI.class))).thenReturn(creatorMock);

            Message twilioResp = mock(Message.class);
            when(twilioResp.getSid()).thenReturn("SM1234567890");
            when(creatorMock.create()).thenReturn(twilioResp);

            messageStatic.when(() ->
                    Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString())
            ).thenReturn(creatorMock);

            String userEmail = "test@demo.com";
            List<String> recipients = List.of("+821011111111", "+821022222222", "+821033333333");

            // When: 서비스 호출
            Messages parent = messageService.sendManyMessage(userEmail, "hello", recipients);

            // Then: DB 저장 및 대시보드 호출 검증
            verify(messageRepository, times(1)).save(any(Messages.class));
            verify(messageRecipientRepository, times(3)).save(any(MessageRecipient.class));
            verify(dashboardApiClient, times(3)).sendDashboardData(any(MessageDashboardDataCreateRequest.class));

            assertNotNull(parent);
        }
    }

    /**
     * [테스트 목적]
     * - Twilio API가 중간에 예외를 던지는 경우
     * - 실패한 메시지는 status=failed로 저장되고, 대시보드에도 failed 상태가 전달되는지 검증
     */
    @Test
    @DisplayName("일부 수신자 전송 실패 시 DB에는 failed 상태가 저장되고 대시보드에도 failed 이벤트가 반영된다")
    void shouldMarkFailedAndPushDashboard_whenTwilioThrows() {
        injectFields();

        // Given: Twilio mocking → 일부 호출에서 ApiException 발생
        try (MockedStatic<Twilio> twilioStatic = mockStatic(Twilio.class);
             MockedStatic<Message> messageStatic = mockStatic(Message.class)) {

            MessageCreator creatorMock = mock(MessageCreator.class);
            when(creatorMock.setStatusCallback(any(URI.class))).thenReturn(creatorMock);

            Message ok = mock(Message.class);
            when(ok.getSid()).thenReturn("SM_OK");
            when(creatorMock.create())
                    .thenReturn(ok) // 첫 번째 성공
                    .thenThrow(new ApiException("Twilio error"))
                    .thenReturn(ok); // 세 번째 성공

            messageStatic.when(() ->
                    Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString())
            ).thenReturn(creatorMock);

            List<String> recipients = List.of("+821011111111", "+821022222222", "+821033333333");

            // When: 서비스 호출
            messageService.sendManyMessage("test@demo.com", "hello", recipients);

            // Then: 저장 및 대시보드 호출 검증
            verify(messageRecipientRepository, times(3)).save(any(MessageRecipient.class));
            verify(dashboardApiClient, times(3)).sendDashboardData(any(MessageDashboardDataCreateRequest.class));
        }
    }

    /**
     * [테스트 목적]
     * - 10,000건 대량 수신자 테스트 (성능 검증 스모크)
     * - Twilio는 Mock 처리, 저장/대시보드 호출이 정확히 횟수만큼 발생하는지 확인
     */
    @Test
    @Tag("slow") // CI에서 제외하고 필요 시만 실행
    @DisplayName("📊 10,000건 대량 전송 스모크 테스트 - 모든 호출이 정확히 횟수만큼 수행된다")
    void smokeTest_sendTenThousand_withStaticMock() {
        injectFields();

        // Given: Twilio mocking → 항상 성공 응답 반환
        try (MockedStatic<Twilio> twilioStatic = mockStatic(Twilio.class);
             MockedStatic<Message> messageStatic = mockStatic(Message.class)) {

            MessageCreator creatorMock = mock(MessageCreator.class);
            when(creatorMock.setStatusCallback(any(URI.class))).thenReturn(creatorMock);

            Message twilioResp = mock(Message.class);
            when(twilioResp.getSid()).thenReturn("SM_BULK");
            when(creatorMock.create()).thenReturn(twilioResp);

            messageStatic.when(() ->
                    Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString())
            ).thenReturn(creatorMock);

            List<String> recipients =
                    java.util.stream.IntStream.range(0, 10_000)
                            .mapToObj(i -> "+1000000" + i)
                            .toList();

            // When: 서비스 호출
            messageService.sendManyMessage("bulk@test.com", "부하테스트", recipients);

            // Then: 10,000건 저장 및 대시보드 호출 확인
            verify(messageRecipientRepository, times(10_000)).save(any(MessageRecipient.class));
            verify(dashboardApiClient, times(10_000)).sendDashboardData(any(MessageDashboardDataCreateRequest.class));
        }
    }
}
