package com.kismet.backend.websocket;

import com.kismet.backend.dto.ChatMessage;
import com.kismet.backend.enums.MessageType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
@Controller
public class ChatWebSocketController {

    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage message) {

        message.setTimestamp(LocalDateTime.now());

        if (message.getMessageType() == null) {
            message.setMessageType(MessageType.CHAT);
        }

        return message;
    }
}
