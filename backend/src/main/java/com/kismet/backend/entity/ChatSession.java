package com.kismet.backend.entity;


import com.kismet.backend.enums.ChatSessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="chat_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String roomId;

    @Column(nullable = false)
    private String userOneGuestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatSessionStatus status;

    private LocalDateTime startedAt;
    private  LocalDateTime endedAt;

    private String endedByGuestId;

}
