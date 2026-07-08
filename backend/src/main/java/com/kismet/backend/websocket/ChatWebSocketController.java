package com.kismet.backend.websocket;

import com.kismet.backend.dto.ChatMessage;
import com.kismet.backend.enums.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(Principal principal, ChatMessage message) {
        if (principal == null) return;

        message.setTimestamp(LocalDateTime.now());
        message.setSenderGuestId(principal.getName());

        if (message.getMessageType() == null) {
            message.setMessageType(MessageType.CHAT);
        }

        messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), message);
    }
}
