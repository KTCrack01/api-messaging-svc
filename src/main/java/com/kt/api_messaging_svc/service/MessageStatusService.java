package com.kt.api_messaging_svc.service;

import com.kt.api_messaging_svc.repository.MessageRecipientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class MessageStatusService {

    private final MessageRecipientRepository recipientRepository;

    @Transactional
    public void handleStatusCallback(String sid,
                                     String status,
                                     String errorCode,
                                     String errorMsg) {

        if (!"delivered".equalsIgnoreCase(status) && !"failed".equalsIgnoreCase(status)
                && !"undelivered".equalsIgnoreCase(status)) {
            return;
        }

        var optionalRecipient = recipientRepository.findByProviderSid(sid);

        if (optionalRecipient.isPresent()) {
            var rec = optionalRecipient.get();

            if ("delivered".equalsIgnoreCase(status)) {
                rec.markDelivered(LocalDateTime.now());
            } else {
                String err = "";
                if (errorCode != null && !errorCode.isBlank()) {
                    err += "[" + errorCode + "] ";
                }
                if (errorMsg != null && !errorMsg.isBlank()) {
                    err += errorMsg;
                }
                rec.markFailed(err);
            }

            recipientRepository.save(rec);
        }
    }
}
