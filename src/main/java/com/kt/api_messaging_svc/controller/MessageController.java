package com.kt.api_messaging_svc.controller;

import com.kt.api_messaging_svc.dto.MessageResponse;
import com.kt.api_messaging_svc.dto.SendMessageRequest;
import com.kt.api_messaging_svc.dto.StatusCallbackRequest;
import com.kt.api_messaging_svc.entity.Messages;
import com.kt.api_messaging_svc.service.MessageService;
import com.kt.api_messaging_svc.service.MessageStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@AllArgsConstructor
@Tag(name = "Messages", description = "메시지 관련 API")
public class MessageController {
    private final MessageService messageService;
    private final MessageStatusService messageStatusService;

    @PostMapping
    @Operation(summary = "메시지 전송", description = "여러 수신자에게 메시지를 전송합니다.")
    @ApiResponse(responseCode = "200", description = "메시지 전송 성공")
    public Messages send(@RequestBody SendMessageRequest request) {
        return messageService.sendManyMessage(
                request.getUserEmail(),
                request.getBody(),
                request.getRecipients()
        );
    }

    @PostMapping("/status")
    @Operation(summary = "메시지 상태 콜백", description = "Twilio에서 메시지 상태 변경 시 호출되는 콜백 엔드포인트입니다.")
    @ApiResponse(responseCode = "200", description = "상태 업데이트 성공")
    public ResponseEntity<Void> statusCallback(@ModelAttribute StatusCallbackRequest req) {
        messageStatusService.handleStatusCallback(req.getMessageSid(),
                req.getMessageStatus(),
                req.getErrorCode(),
                req.getErrorMessage());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "사용자 메시지 조회", description = "특정 사용자의 메시지 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "메시지 조회 성공")
    public List<MessageResponse> findByUserEmail(
            @Parameter(description = "조회할 사용자의 이메일 주소", required = true)
            @RequestParam String userEmail
    ) {
        return messageService.getMessagesByUserEmail(userEmail);
    }
}
