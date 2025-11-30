package org.example.notificationsvc.service;

import com.twilio.exception.ApiException;
import org.example.notificationsvc.exception.NotificationException;
import org.example.notificationsvc.model.Notification;
import org.example.notificationsvc.model.NotificationStatus;
import org.example.notificationsvc.repository.NotificationRepository;
import org.example.notificationsvc.web.dto.SmsRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class NotificationServiceUTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SmsService smsService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendSms_shouldSuccessfullySendIfAllDataIsValid() {
        SmsRequest smsRequest = SmsRequest.builder()
                .message("Test message")
                .phoneNumber("+359123456789")
                .senderId(UUID.randomUUID())
                .build();

        doNothing().when(smsService).sendNotification(smsRequest.getPhoneNumber(), smsRequest.getMessage());
        ArgumentCaptor<Notification> smsCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.save(any())).thenAnswer(invocation ->  invocation.getArgument(0));

        ResponseEntity<String> responseEntity = notificationService.sendSms(smsRequest);

        verify(notificationRepository).save(smsCaptor.capture());
        Notification smsNotification = smsCaptor.getValue();
        assertEquals("Test message",  smsNotification.getMessage());
        assertEquals("+359123456789",  smsNotification.getContactInfo());
        assertEquals(NotificationStatus.SUCCEEDED, smsNotification.getStatus());
        assertEquals(smsRequest.getSenderId(), smsNotification.getUserId());
        assertEquals("SMS sent to +359123456789", responseEntity.getBody());
    }

    @Test
    void sendSms_shouldSetStatusToFailedIfSendingFails() {
        SmsRequest smsRequest = SmsRequest.builder()
                .message("Test message")
                .phoneNumber("+359123456789")
                .senderId(UUID.randomUUID())
                .build();

        ApiException apiException = new ApiException("Twilio API error");
        doThrow(apiException).when(smsService).sendNotification(smsRequest.getPhoneNumber(), smsRequest.getMessage());
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationException exception = assertThrows(NotificationException.class, () -> {
            notificationService.sendSms(smsRequest);
        });

        verify(smsService).sendNotification(smsRequest.getPhoneNumber(), smsRequest.getMessage());
        verify(notificationRepository, times(1)).save(notificationCaptor.capture());
        Notification savedNotification = notificationCaptor.getValue();
        
        assertEquals(NotificationStatus.FAILED, savedNotification.getStatus());
        assertEquals("Test message", savedNotification.getMessage());
        assertEquals("+359123456789", savedNotification.getContactInfo());
        assertEquals(smsRequest.getSenderId(), savedNotification.getUserId());
        assertTrue(exception.getMessage().contains("Failed to send SMS to " + smsRequest.getPhoneNumber()));
        assertEquals(apiException, exception.getCause());
    }

}
