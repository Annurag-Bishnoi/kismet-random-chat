package com.kismet.backend.dto;

import com.kismet.backend.enums.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    private String senderGuestId;

    private String senderName;

    private String content;

    private MessageType messageType;

    private LocalDateTime timestamp;

}
