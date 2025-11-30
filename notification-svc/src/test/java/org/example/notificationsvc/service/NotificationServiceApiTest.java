package org.example.notificationsvc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.exception.ApiException;
import org.example.notificationsvc.exception.NotificationException;
import org.example.notificationsvc.web.NotificationController;
import org.example.notificationsvc.web.dto.SmsRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
public class NotificationServiceApiTest {

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sendSms_shouldReturn200OkIfInputDataIsValid() throws Exception {
        SmsRequest smsRequest = SmsRequest.builder()
                .phoneNumber("+359123456789")
                .message("Test message")
                .senderId(UUID.randomUUID())
                .build();

        when(notificationService.sendSms(any(SmsRequest.class)))
                .thenReturn(ResponseEntity.ok("SMS sent to " + smsRequest.getPhoneNumber()));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smsRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SMS sent to " + smsRequest.getPhoneNumber())));
    }

    @Test
    void sendSms_shouldReturn400WhenPhoneNumberIsMissing() throws Exception {
        SmsRequest smsRequest = SmsRequest.builder()
                .message("Test message")
                .senderId(UUID.randomUUID())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smsRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Phone number is required"));
    }

    @Test
    void sendSms_shouldReturn400WhenMessageIsMissing() throws Exception {
        SmsRequest smsRequest = SmsRequest.builder()
                .phoneNumber("+359123456789")
                .senderId(UUID.randomUUID())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smsRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Message is required"));
    }

    @Test
    void sendSms_shouldReturn400WhenSenderIdIsMissing() throws Exception {
        SmsRequest smsRequest = SmsRequest.builder()
                .phoneNumber("+359123456789")
                .message("Test message")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smsRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.senderId").value("Sender ID is required"));
    }

    @Test
    void sendSms_shouldReturn400WhenNotificationExceptionIsThrown() throws Exception {
        SmsRequest smsRequest = SmsRequest.builder()
                .phoneNumber("+359123456789")
                .message("Test message")
                .senderId(UUID.randomUUID())
                .build();

        ApiException apiException = new ApiException("Twilio API error");
        NotificationException notificationException = new NotificationException("Failed to send SMS", apiException);

        when(notificationService.sendSms(any(SmsRequest.class)))
                .thenThrow(notificationException);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smsRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Failed to send SMS"));
    }
}
