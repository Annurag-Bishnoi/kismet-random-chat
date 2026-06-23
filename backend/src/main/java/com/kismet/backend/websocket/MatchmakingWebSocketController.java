package com.kismet.backend.websocket;

import com.kismet.backend.dto.MatchRequest;
import com.kismet.backend.service.MatchmakingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MatchmakingWebSocketController {

    private final MatchmakingService matchmakingService;

    @MessageMapping("/chat.start")
    public void startChat(MatchRequest request) {
        matchmakingService.findMatch(request.getGuestId());
    }
}