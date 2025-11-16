package org.example.notificationsvc.web;

import org.example.notificationsvc.model.Notification;
import org.example.notificationsvc.service.NotificationService;
import org.example.notificationsvc.web.dto.SmsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/sms")
    public ResponseEntity<String> sendSms(@RequestBody SmsRequest smsRequest) {

        return notificationService.sendSms(smsRequest);

    }

    @GetMapping("/sms")
    public ResponseEntity<List<Notification>> getAllSmsBySender(@RequestParam("userId") UUID senderId) {

        List<Notification> allBySenderId = notificationService.findAllBySenderId(senderId);

        return ResponseEntity.ok(allBySenderId);
    }

}
