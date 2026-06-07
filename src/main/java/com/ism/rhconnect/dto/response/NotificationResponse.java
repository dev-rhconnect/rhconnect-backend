package com.ism.rhconnect.dto.response;

import com.ism.rhconnect.entity.Notification;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Notification.Type type;
    private String message;
    private boolean lu;
    private LocalDateTime dateEnvoi;
}
