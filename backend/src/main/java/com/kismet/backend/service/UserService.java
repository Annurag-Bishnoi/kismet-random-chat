package com.kismet.backend.service;

import com.kismet.backend.entity.User;
import com.kismet.backend.enums.GuestStatus;
import com.kismet.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getOrCreateUser(String email, String displayName) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    user.setLastSeenAt(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .displayName(displayName != null ? displayName : "User")
                            .status(GuestStatus.ONLINE)
                            .createdAt(LocalDateTime.now())
                            .lastSeenAt(LocalDateTime.now())
                            .build();
                    return userRepository.save(newUser);
                });
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public void updateUserStatus(String email, GuestStatus status) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        user.setStatus(status);
        user.setLastSeenAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
