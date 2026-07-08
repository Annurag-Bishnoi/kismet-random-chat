package com.kismet.backend.repository;

import com.kismet.backend.entity.User;
import com.kismet.backend.enums.GuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByStatus(GuestStatus status);
    boolean existsByEmail(String email);
}
