package com.kt.api_messaging_svc.controller;

import com.kt.api_messaging_svc.dto.MessageResponse;
import com.kt.api_messaging_svc.dto.SendMessageRequest;
import com.kt.api_messaging_svc.dto.StatusCallbackRequest;
import com.kt.api_messaging_svc.entity.Messages;
import com.kt.api_messaging_svc.service.MessageService;
import com.kt.api_messaging_svc.service.MessageStatusService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@AllArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final MessageStatusService messageStatusService;

    @PostMapping
    public Messages send(@RequestBody SendMessageRequest request) {
        return messageService.sendManyMessage(
                request.getUserEmail(),
                request.getBody(),
                request.getRecipients()
        );
    }

    @PostMapping("/status")
    public ResponseEntity<Void> statusCallback(@ModelAttribute StatusCallbackRequest req) {
        messageStatusService.handleStatusCallback(req.getMessageSid(),
                req.getMessageStatus(),
                req.getErrorCode(),
                req.getErrorMessage());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<MessageResponse> findByUserEmail(
            @RequestParam String userEmail
    ) {
        return messageService.getMessagesByUserEmail(userEmail);
    }
}
