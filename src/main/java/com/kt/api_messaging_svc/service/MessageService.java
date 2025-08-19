package com.kt.api_messaging_svc.service;

import com.kt.api_messaging_svc.entity.MessageRecipient;
import com.kt.api_messaging_svc.entity.Messages;
import com.kt.api_messaging_svc.repository.MessageRecipientRepository;
import com.kt.api_messaging_svc.repository.MessageRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final MessageRecipientRepository messageRecipientRepository;
    // Find your Account SID and Auth Token at twilio.com/console
    // and set the environment variables. See http://twil.io/secure
    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    public MessageService(MessageRepository messageRepository, MessageRecipientRepository messageRecipientRepository) {
        this.messageRepository = messageRepository;
        this.messageRecipientRepository = messageRecipientRepository;
    }

    public void sendMessage() {
        System.out.println(accountSid);
        System.out.println(authToken);
        Twilio.init(accountSid, authToken);
        Message message = Message
                .creator(new com.twilio.type.PhoneNumber("+821062987361"),
                        new com.twilio.type.PhoneNumber("+15177656650"),
                        "성공")
                .create();

        System.out.println(message.getBody());

        System.out.println("Message SID: " + message.getSid());
        System.out.println("Status: " + message.getStatus()); // queue
    }

    public void sendManyMessage() {
        Twilio.init(accountSid, authToken);

        String from = "+15177656650"; // 발신 번호
        String[] recipients = { "+821051893865", "+821097773865", "+821028066048"};

        for (String to : recipients) {
            Message message = Message.creator(
                    new com.twilio.type.PhoneNumber(to),
                    new com.twilio.type.PhoneNumber(from),
                    "이성무 술깨"
            ).create();

            System.out.println("Sent to " + to + ": " + message.getSid());
        }
    }


    @Transactional
    public Messages sendManyMessage(String userEmail, String body, String from, List<String> recipients) {
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
            // Twilio 전송
            com.twilio.rest.api.v2010.account.Message twilioMsg =
                    com.twilio.rest.api.v2010.account.Message.creator(
                            new com.twilio.type.PhoneNumber(to),
                            new com.twilio.type.PhoneNumber(from),
                            body
                    ).create();

            // Twilio 현재 상태 (queued/sending/sent/…)
            String status = twilioMsg.getStatus() != null ? twilioMsg.getStatus().toString() : null;

            // 수신자 레코드 저장
            MessageRecipient recipient = MessageRecipient.builder()
                    .messages(messagesEntity)
                    .phoneNum(to)
                    .sendAt(LocalDateTime.now())
                    .deliveredAt(null)     // Webhook으로 후속 업데이트 권장
                    .status(status)
                    .build();

            messageRecipientRepository.save(recipient);

            System.out.println("Sent to " + to + " | sid=" + twilioMsg.getSid() + " | status=" + status);
        }

        return messagesEntity;
    }
}
