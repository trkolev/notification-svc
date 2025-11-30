package org.example.notificationsvc.service;

import com.twilio.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationsvc.exception.NotificationException;
import org.example.notificationsvc.model.Notification;
import org.example.notificationsvc.model.NotificationStatus;
import org.example.notificationsvc.repository.NotificationRepository;
import org.example.notificationsvc.web.dto.SmsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SmsService smsService;

    public NotificationService(NotificationRepository notificationRepository, SmsService smsService) {
        this.notificationRepository = notificationRepository;
        this.smsService = smsService;
    }

    public ResponseEntity<String> sendSms(SmsRequest smsRequest) {
        Notification notification = Notification.builder()
                .message(smsRequest.getMessage())
                .contactInfo(smsRequest.getPhoneNumber())
                .createdAt(LocalDateTime.now())
                .status(NotificationStatus.SUCCEEDED)
                .userId(smsRequest.getSenderId())
                .isDeleted(false)
                .build();

        try {
            smsService.sendNotification(smsRequest.getPhoneNumber(), smsRequest.getMessage());
            notificationRepository.save(notification);
            return ResponseEntity.ok("SMS sent to " + smsRequest.getPhoneNumber());
        } catch (ApiException e) {
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            log.error("Twilio API error: " + e.getMessage());
            throw new NotificationException("Failed to send SMS to " + smsRequest.getPhoneNumber() + ": " + e.getMessage(), e);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            log.error("Twilio exception: " + e.getMessage());
            throw new NotificationException("Internal error while sending SMS to " + smsRequest.getPhoneNumber() + ": " + e.getMessage(), e);
        }
    }

    public List<Notification> findAllBySenderId(UUID senderId) {
        return notificationRepository.findAllByUserIdAndIsDeletedIsFalse(senderId);
    }

    public void deleteAllBySenderId(UUID senderId) {
        notificationRepository.findAllByUserIdAndIsDeletedIsFalse(senderId).forEach(notification -> {
            notification.setDeleted(true);
            notificationRepository.save(notification);
        });
    }
}
