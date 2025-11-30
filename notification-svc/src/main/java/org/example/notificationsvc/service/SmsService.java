package org.example.notificationsvc.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.phone-number}")
    private String phoneNumber;

    public void sendNotification(String number, String message) {
        Message.creator(new PhoneNumber(number), new PhoneNumber(phoneNumber), message).create();
    }
}
