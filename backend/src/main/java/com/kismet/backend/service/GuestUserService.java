package com.kismet.backend.service;

import com.kismet.backend.dto.GuestResponse;
import com.kismet.backend.entity.GuestUser;
import com.kismet.backend.enums.GuestStatus;
import com.kismet.backend.repository.GuestUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuestUserService {

    private final GuestUserRepository guestUserRepository;

    public GuestResponse createGuestUser() {

        String guestId = "GUEST-" + UUID.randomUUID().toString().substring(0, 8);

        GuestUser guestUser = GuestUser.builder()
                .guestId(guestId)
                .displayName("Stranger")
                .status(GuestStatus.ONLINE)
                .createdAt(LocalDateTime.now())
                .lastSeenAt(LocalDateTime.now())
                .build();

        GuestUser savedUser = guestUserRepository.save(guestUser);

        return GuestResponse.builder()
                .guestId(savedUser.getGuestId())
                .displayName(savedUser.getDisplayName())
                .status(savedUser.getStatus())
                .build();
    }



    public GuestResponse getGuestByGuestId(String guestId) {

        GuestUser guestUser = guestUserRepository.findByGuestId(guestId)
                .orElseThrow(() -> new RuntimeException("Guest user not found"));

        return GuestResponse.builder()
                .guestId(guestUser.getGuestId())
                .displayName(guestUser.getDisplayName())
                .status(guestUser.getStatus())
                .build();
    }



    public void updateGuestStatus(String guestId, GuestStatus status) {

        GuestUser guestUser = guestUserRepository.findByGuestId(guestId)
                .orElseThrow(() -> new RuntimeException("Guest user not found"));

        guestUser.setStatus(status);
        guestUser.setLastSeenAt(LocalDateTime.now());

        guestUserRepository.save(guestUser);
    }


}
