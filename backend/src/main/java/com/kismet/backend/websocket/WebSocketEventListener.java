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
    private final Map<String, String> sessionToGuestMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String destination = headers.getDestination();
        String sessionId = headers.getSessionId();

        if (destination != null && destination.startsWith("/topic/match/")) {
            String guestId = destination.substring("/topic/match/".length());
            sessionToGuestMap.put(sessionId, guestId);
            log.info("Mapped sessionId {} to guestId {}", sessionId, guestId);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String guestId = sessionToGuestMap.remove(sessionId);

        if (guestId != null) {
            log.info("Guest {} disconnected", guestId);
            matchmakingService.handleUserDisconnect(guestId);
        }
    }
}
