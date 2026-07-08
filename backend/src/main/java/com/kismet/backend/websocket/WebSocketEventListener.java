package com.kismet.backend.websocket;

import com.kismet.backend.service.MatchmakingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final MatchmakingService matchmakingService;
    private final Map<String, String> sessionToEmailMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String destination = headers.getDestination();
        String sessionId = headers.getSessionId();

        if (destination != null && destination.startsWith("/topic/match/")) {
            String email = destination.substring("/topic/match/".length());
            sessionToEmailMap.put(sessionId, email);
            log.info("Mapped sessionId {} to email {}", sessionId, email);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String email = sessionToEmailMap.remove(sessionId);

        if (email != null) {
            log.info("User {} disconnected", email);
            matchmakingService.handleUserDisconnect(email);
        }
    }
}
