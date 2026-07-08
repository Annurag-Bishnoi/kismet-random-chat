package com.kismet.backend.websocket;

import com.kismet.backend.dto.ChatLeaveRequest;
import com.kismet.backend.service.MatchmakingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MatchmakingWebSocketController {

    private final MatchmakingService matchmakingService;

    @MessageMapping("/chat.start")
    public void startChat(Principal principal) {
        if (principal == null) return;
        matchmakingService.findMatch(principal.getName());
    }

    @MessageMapping("/chat.end")
    public void endChat(Principal principal, ChatLeaveRequest request) {
        if (principal == null) return;
        matchmakingService.endChat(principal.getName(), request.getRoomId());
    }
}