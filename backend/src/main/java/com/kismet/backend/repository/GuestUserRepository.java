package com.kismet.backend.repository;

import com.kismet.backend.entity.GuestUser;
import com.kismet.backend.enums.GuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuestUserRepository extends JpaRepository<GuestUser,Long> {

    Optional<GuestUser> findByGuestId(String guestId);
    List<GuestUser> findByStatus(GuestStatus status);

    boolean existsByGuestId(String guestId);

}
