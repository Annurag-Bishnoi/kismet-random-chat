package com.kismet.backend.service;

import com.kismet.backend.dto.MatchResponse;
import com.kismet.backend.dto.ChatMessage;
import com.kismet.backend.entity.ChatSession;
import com.kismet.backend.enums.ChatSessionStatus;
import com.kismet.backend.enums.GuestStatus;
import com.kismet.backend.enums.MatchStatus;
import com.kismet.backend.enums.MessageType;
import com.kismet.backend.repository.ChatSessionRepository;
import com.kismet.backend.repository.UserRepository;
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

@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Queue<String> waitingQueue = new ConcurrentLinkedQueue<>();
    private final Set<String> waitingUsers = ConcurrentHashMap.newKeySet();

    public void findMatch(String email) {

        if (email == null || email.isBlank()) {
            return;
        }

        boolean userExists = userRepository.existsByEmail(email);

        if (!userExists) {
            sendToUser(email, MatchResponse.builder()
                    .status(MatchStatus.FAILED)
                    .message("Invalid user")
                    .guestId(email)
                    .build());
            return;
        }

        if (waitingUsers.contains(email)) {
            sendToUser(email, MatchResponse.builder()
                    .status(MatchStatus.WAITING)
                    .message("Already waiting for a stranger...")
                    .guestId(email)
                    .build());
            return;
        }

        String waitingEmail = getValidWaitingUser(email);

        if (waitingEmail == null) {
            addToWaitingQueue(email);
            return;
        }

        createMatch(waitingEmail, email);
    }

    private String getValidWaitingUser(String currentEmail) {

        while (!waitingQueue.isEmpty()) {
            String waitingEmail = waitingQueue.poll();
            waitingUsers.remove(waitingEmail);

            if (!waitingEmail.equals(currentEmail)
                    && userRepository.existsByEmail(waitingEmail)) {
                return waitingEmail;
            }
        }

        return null;
    }

    private void addToWaitingQueue(String email) {

        waitingQueue.offer(email);
        waitingUsers.add(email);

        userService.updateUserStatus(email, GuestStatus.WAITING);

        sendToUser(email, MatchResponse.builder()
                .status(MatchStatus.WAITING)
                .message("Waiting for a stranger...")
                .guestId(email)
                .build());
    }

    private void createMatch(String userOneEmail, String userTwoEmail) {

        String roomId = "ROOM-" + UUID.randomUUID().toString().substring(0, 8);

        ChatSession chatSession = ChatSession.builder()
                .roomId(roomId)
                .userOneGuestId(userOneEmail)
                .userTwoGuestId(userTwoEmail)
                .status(ChatSessionStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .build();

        chatSessionRepository.save(chatSession);

        userService.updateUserStatus(userOneEmail, GuestStatus.CHATTING);
        userService.updateUserStatus(userTwoEmail, GuestStatus.CHATTING);

        sendToUser(userOneEmail, MatchResponse.builder()
                .status(MatchStatus.MATCHED)
                .message("Stranger found")
                .roomId(roomId)
                .guestId(userOneEmail)
                .strangerGuestId(userTwoEmail)
                .build());

        sendToUser(userTwoEmail, MatchResponse.builder()
                .status(MatchStatus.MATCHED)
                .message("Stranger found")
                .roomId(roomId)
                .guestId(userTwoEmail)
                .strangerGuestId(userOneEmail)
                .build());
    }

    private void sendToUser(String email, MatchResponse response) {
        messagingTemplate.convertAndSend("/topic/match/" + email, response);
    }

    public void endChat(String email, String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return;
        }

        chatSessionRepository.findByRoomId(roomId).ifPresent(session -> {
            if (session.getStatus() == ChatSessionStatus.ACTIVE) {
                session.setStatus(ChatSessionStatus.ENDED);
                session.setEndedAt(LocalDateTime.now());
                session.setEndedByGuestId(email);
                chatSessionRepository.save(session);

                // Update users' status to ONLINE
                userService.updateUserStatus(session.getUserOneGuestId(), GuestStatus.ONLINE);
                userService.updateUserStatus(session.getUserTwoGuestId(), GuestStatus.ONLINE);

                // Notify both users in the room that the chat has ended
                ChatMessage leaveMessage = ChatMessage.builder()
                        .senderGuestId(email)
                        .content("Stranger has left the chat.")
                        .messageType(MessageType.LEAVE)
                        .timestamp(LocalDateTime.now())
                        .roomId(roomId)
                        .build();

                messagingTemplate.convertAndSend("/topic/room/" + roomId, leaveMessage);
            }
        });
    }

    public void handleUserDisconnect(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        // 1. Remove from waiting queue if present
        waitingUsers.remove(email);
        waitingQueue.remove(email);

        // 2. Find any active chat sessions for this user and end them
        List<ChatSession> activeSessions = chatSessionRepository.findByStatus(ChatSessionStatus.ACTIVE);
        for (ChatSession session : activeSessions) {
            if (session.getUserOneGuestId().equals(email) || session.getUserTwoGuestId().equals(email)) {
                session.setStatus(ChatSessionStatus.ENDED);
                session.setEndedAt(LocalDateTime.now());
                session.setEndedByGuestId(email);
                chatSessionRepository.save(session);

                String partnerEmail = session.getUserOneGuestId().equals(email)
                        ? session.getUserTwoGuestId()
                        : session.getUserOneGuestId();

                // Update partner status to ONLINE
                userService.updateUserStatus(partnerEmail, GuestStatus.ONLINE);

                // Notify partner
                ChatMessage leaveMessage = ChatMessage.builder()
                        .senderGuestId(email)
                        .content("Stranger has disconnected.")
                        .messageType(MessageType.LEAVE)
                        .timestamp(LocalDateTime.now())
                        .roomId(session.getRoomId())
                        .build();

                messagingTemplate.convertAndSend("/topic/room/" + session.getRoomId(), leaveMessage);
            }
        }

        // 3. Mark the user as OFFLINE in the database
        userService.updateUserStatus(email, GuestStatus.OFFLINE);
    }
}