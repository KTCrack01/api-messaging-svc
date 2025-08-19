package com.kt.api_messaging_svc.controller;


import com.kt.api_messaging_svc.dto.SendMessageRequest;
import com.kt.api_messaging_svc.entity.Messages;
import com.kt.api_messaging_svc.service.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
@AllArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public Messages send(@RequestBody SendMessageRequest request) {
        return messageService.sendManyMessage(
                request.getUserEmail(),
                request.getBody(),
                request.getFrom(),
                request.getRecipients()
        );
    }
}
