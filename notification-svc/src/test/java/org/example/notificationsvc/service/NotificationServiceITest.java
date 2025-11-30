package org.example.notificationsvc.service;

import org.example.notificationsvc.exception.NotificationException;
import org.example.notificationsvc.model.Notification;
import org.example.notificationsvc.model.NotificationStatus;
import org.example.notificationsvc.repository.NotificationRepository;
import org.example.notificationsvc.web.dto.SmsRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest()
public class NotificationServiceITest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private SmsService smsService;

    @Test
    void sendSms_happyPath() {
        SmsRequest smsRequest = SmsRequest.builder()
                .phoneNumber("+359988833954")
                .message("Test message")
                .senderId(UUID.randomUUID())
                .build();

        ResponseEntity<String> responseEntity = notificationService.sendSms(smsRequest);

        assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(responseEntity.getBody()).isEqualTo("SMS sent to +359988833954");
    }

    @Test
    void findAllBySenderId_shouldReturnUserNotifications() {
        UUID senderId1 = UUID.randomUUID();
        UUID senderId2 = UUID.randomUUID();

        Notification notification1 = Notification.builder()
                .message("Message 1")
                .contactInfo("+359123456789")
                .createdAt(LocalDateTime.now())
                .status(NotificationStatus.SUCCEEDED)
                .userId(senderId1)
                .isDeleted(false)
                .build();

        Notification notification2 = Notification.builder()
                .message("Message 2")
                .contactInfo("+359987654321")
                .createdAt(LocalDateTime.now())
                .status(NotificationStatus.FAILED)
                .userId(senderId1)
                .isDeleted(false)
                .build();

        Notification notification3 = Notification.builder()
                .message("Message 3")
                .contactInfo("+359111111111")
                .createdAt(LocalDateTime.now())
                .status(NotificationStatus.SUCCEEDED)
                .userId(senderId2)
                .isDeleted(false)
                .build();

        Notification deletedNotification = Notification.builder()
                .message("Deleted message")
                .contactInfo("+359222222222")
                .createdAt(LocalDateTime.now())
                .status(NotificationStatus.SUCCEEDED)
                .userId(senderId1)
                .isDeleted(true)
                .build();

        Notification savedNotification1 = notificationRepository.save(notification1);
        Notification savedNotification2 = notificationRepository.save(notification2);
        Notification savedNotification3 = notificationRepository.save(notification3);
        Notification savedDeletedNotification = notificationRepository.save(deletedNotification);

        List<Notification> result = notificationService.findAllBySenderId(senderId1);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Notification::getId)
                .containsExactlyInAnyOrder(savedNotification1.getId(), savedNotification2.getId());
        assertThat(result).extracting(Notification::getId)
                .doesNotContain(savedNotification3.getId());
        assertThat(result).extracting(Notification::getId)
                .doesNotContain(savedDeletedNotification.getId());
        assertThat(result).extracting(Notification::getMessage)
                .containsExactlyInAnyOrder("Message 1", "Message 2");
        assertThat(result).extracting(Notification::getUserId)
                .containsOnly(senderId1);
        assertThat(result).extracting(Notification::isDeleted)
                .containsOnly(false);
    }

    @Test
    void sendSms_shouldSaveNotificationWithFailedStatusAndThrowNotificationExceptionWhenGeneralExceptionOccurs() {
        SmsRequest smsRequest = SmsRequest.builder()
                .phoneNumber("+359123456789")
                .message("Test message")
                .senderId(UUID.randomUUID())
                .build();

        RuntimeException generalException = new RuntimeException("Connection timeout");
        doThrow(generalException).when(smsService).sendNotification(anyString(), anyString());

        assertThatThrownBy(() -> notificationService.sendSms(smsRequest))
                .isInstanceOf(NotificationException.class)
                .hasMessageContaining("Internal error while sending SMS to " + smsRequest.getPhoneNumber())
                .hasMessageContaining("Connection timeout")
                .hasCause(generalException);

        List<Notification> savedNotifications = notificationRepository.findAll();
        assertThat(savedNotifications).hasSize(1);
        Notification savedNotification = savedNotifications.get(0);
        assertThat(savedNotification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(savedNotification.getMessage()).isEqualTo(smsRequest.getMessage());
        assertThat(savedNotification.getContactInfo()).isEqualTo(smsRequest.getPhoneNumber());
        assertThat(savedNotification.getUserId()).isEqualTo(smsRequest.getSenderId());
        assertThat(savedNotification.isDeleted()).isFalse();
    }
}
