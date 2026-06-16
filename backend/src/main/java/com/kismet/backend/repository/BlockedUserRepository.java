package com.kismet.backend.repository;

import com.kismet.backend.entity.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockedUserRepository extends JpaRepository<BlockedUser,Long> {
    List<BlockedUser> findByBlockerGuestId(String blockerGuestId);
}
