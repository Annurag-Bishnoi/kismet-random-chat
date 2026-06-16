package com.kismet.backend.repository;

import com.kismet.backend.entity.ChatSession;
import com.kismet.backend.enums.ChatSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession,Long> {

    Optional<ChatSession> findByRoomId(String roomId);
    List<ChatSession> findByStatus(ChatSessionStatus status);

}
