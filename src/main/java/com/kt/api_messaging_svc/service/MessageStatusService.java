package com.kt.api_messaging_svc.service;

import com.kt.api_messaging_svc.dto.MessageDashboardDataCreateRequest;
import com.kt.api_messaging_svc.dto.StatusUpdateRequest;
import com.kt.api_messaging_svc.repository.MessageRecipientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class MessageStatusService {

    private final MessageRecipientRepository recipientRepository;
    private final DashboardApiClient dashboardApiClient;

    @Transactional
    public void handleStatusCallback(String sid,
                                     String status,
                                     String errorCode,
                                     String errorMsg) {

        var optionalRecipient = recipientRepository.findByProviderSid(sid);

        if (optionalRecipient.isPresent()) {
            var rec = optionalRecipient.get();

            if ("delivered".equalsIgnoreCase(status)) {
                rec.markDelivered(LocalDateTime.now());
                // 상태변경 api 호출

                StatusUpdateRequest req =
                        new StatusUpdateRequest(sid, "delivered");
                dashboardApiClient.sendDashboardData(req);
            } else {
                String err = "";
                if (errorCode != null && !errorCode.isBlank()) {
                    err += "[" + errorCode + "] ";
                }
                if (errorMsg != null && !errorMsg.isBlank()) {
                    err += errorMsg;
                }
                rec.markFailed(err);

                StatusUpdateRequest req =
                        new StatusUpdateRequest(sid, "failed");
                dashboardApiClient.sendDashboardData(req);
            }

            recipientRepository.save(rec);
        }
    }
}
