package com.kismet.backend.websocket;

import com.kismet.backend.dto.ChatMessage;
import com.kismet.backend.enums.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());

        if (message.getMessageType() == null) {
            message.setMessageType(MessageType.CHAT);
        }

        messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), message);
    }
}
