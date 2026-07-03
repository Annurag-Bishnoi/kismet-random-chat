package com.kismet.backend.service;

import com.kismet.backend.dto.MatchResponse;
import com.kismet.backend.entity.ChatSession;
import com.kismet.backend.enums.ChatSessionStatus;
import com.kismet.backend.enums.GuestStatus;
import com.kismet.backend.enums.MatchStatus;
import com.kismet.backend.repository.ChatSessionRepository;
import com.kismet.backend.repository.GuestUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.kismet.backend.dto.ChatMessage;
import com.kismet.backend.enums.MessageType;

@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private final GuestUserRepository guestUserRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final GuestUserService guestUserService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Queue<String> waitingQueue = new ConcurrentLinkedQueue<>();
    private final Set<String> waitingUsers = ConcurrentHashMap.newKeySet();

    public void findMatch(String guestId) {

        if (guestId == null || guestId.isBlank()) {
            return;
        }

        boolean guestExists = guestUserRepository.existsByGuestId(guestId);

        if (!guestExists) {
            sendToGuest(guestId, MatchResponse.builder()
                    .status(MatchStatus.FAILED)
                    .message("Invalid guest user")
                    .guestId(guestId)
                    .build());
            return;
        }

        if (waitingUsers.contains(guestId)) {
            sendToGuest(guestId, MatchResponse.builder()
                    .status(MatchStatus.WAITING)
                    .message("Already waiting for a stranger...")
                    .guestId(guestId)
                    .build());
            return;
        }

        String waitingGuestId = getValidWaitingGuest(guestId);

        if (waitingGuestId == null) {
            addToWaitingQueue(guestId);
            return;
        }

        createMatch(waitingGuestId, guestId);
    }

    private String getValidWaitingGuest(String currentGuestId) {

        while (!waitingQueue.isEmpty()) {
            String waitingGuestId = waitingQueue.poll();
            waitingUsers.remove(waitingGuestId);

            if (!waitingGuestId.equals(currentGuestId)
                    && guestUserRepository.existsByGuestId(waitingGuestId)) {
                return waitingGuestId;
            }
        }

        return null;
    }

    private void addToWaitingQueue(String guestId) {

        waitingQueue.offer(guestId);
        waitingUsers.add(guestId);

        guestUserService.updateGuestStatus(guestId, GuestStatus.WAITING);

        sendToGuest(guestId, MatchResponse.builder()
                .status(MatchStatus.WAITING)
                .message("Waiting for a stranger...")
                .guestId(guestId)
                .build());
    }

    private void createMatch(String userOneGuestId, String userTwoGuestId) {

        String roomId = "ROOM-" + UUID.randomUUID().toString().substring(0, 8);

        ChatSession chatSession = ChatSession.builder()
                .roomId(roomId)
                .userOneGuestId(userOneGuestId)
                .userTwoGuestId(userTwoGuestId)
                .status(ChatSessionStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .build();

        chatSessionRepository.save(chatSession);

        guestUserService.updateGuestStatus(userOneGuestId, GuestStatus.CHATTING);
        guestUserService.updateGuestStatus(userTwoGuestId, GuestStatus.CHATTING);

        sendToGuest(userOneGuestId, MatchResponse.builder()
                .status(MatchStatus.MATCHED)
                .message("Stranger found")
                .roomId(roomId)
                .guestId(userOneGuestId)
                .strangerGuestId(userTwoGuestId)
                .build());

        sendToGuest(userTwoGuestId, MatchResponse.builder()
                .status(MatchStatus.MATCHED)
                .message("Stranger found")
                .roomId(roomId)
                .guestId(userTwoGuestId)
                .strangerGuestId(userOneGuestId)
                .build());
    }

    private void sendToGuest(String guestId, MatchResponse response) {
        messagingTemplate.convertAndSend("/topic/match/" + guestId, response);
    }

    public void endChat(String guestId, String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return;
        }

        chatSessionRepository.findByRoomId(roomId).ifPresent(session -> {
            if (session.getStatus() == ChatSessionStatus.ACTIVE) {
                session.setStatus(ChatSessionStatus.ENDED);
                session.setEndedAt(LocalDateTime.now());
                session.setEndedByGuestId(guestId);
                chatSessionRepository.save(session);

                // Update users' status to ONLINE
                guestUserService.updateGuestStatus(session.getUserOneGuestId(), GuestStatus.ONLINE);
                guestUserService.updateGuestStatus(session.getUserTwoGuestId(), GuestStatus.ONLINE);

                // Notify both users in the room that the chat has ended
                ChatMessage leaveMessage = ChatMessage.builder()
                        .senderGuestId(guestId)
                        .content("Stranger has left the chat.")
                        .messageType(MessageType.LEAVE)
                        .timestamp(LocalDateTime.now())
                        .roomId(roomId)
                        .build();

                messagingTemplate.convertAndSend("/topic/room/" + roomId, leaveMessage);
            }
        });
    }

    public void handleUserDisconnect(String guestId) {
        if (guestId == null || guestId.isBlank()) {
            return;
        }

        // 1. Remove from waiting queue if present
        waitingUsers.remove(guestId);
        waitingQueue.remove(guestId);

        // 2. Find any active chat sessions for this user and end them
        List<ChatSession> activeSessions = chatSessionRepository.findByStatus(ChatSessionStatus.ACTIVE);
        for (ChatSession session : activeSessions) {
            if (session.getUserOneGuestId().equals(guestId) || session.getUserTwoGuestId().equals(guestId)) {
                session.setStatus(ChatSessionStatus.ENDED);
                session.setEndedAt(LocalDateTime.now());
                session.setEndedByGuestId(guestId);
                chatSessionRepository.save(session);

                String partnerGuestId = session.getUserOneGuestId().equals(guestId)
                        ? session.getUserTwoGuestId()
                        : session.getUserOneGuestId();

                // Update partner status to ONLINE
                guestUserService.updateGuestStatus(partnerGuestId, GuestStatus.ONLINE);

                // Notify partner
                ChatMessage leaveMessage = ChatMessage.builder()
                        .senderGuestId(guestId)
                        .content("Stranger has disconnected.")
                        .messageType(MessageType.LEAVE)
                        .timestamp(LocalDateTime.now())
                        .roomId(session.getRoomId())
                        .build();

                messagingTemplate.convertAndSend("/topic/room/" + session.getRoomId(), leaveMessage);
            }
        }

        // 3. Mark the user as OFFLINE in the database
        guestUserService.updateGuestStatus(guestId, GuestStatus.OFFLINE);
    }
}