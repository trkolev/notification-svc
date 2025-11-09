package org.example.notificationsvc.web.dto;


import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {

    private String phoneNumber;

    private String message;

    private UUID senderId;

}
