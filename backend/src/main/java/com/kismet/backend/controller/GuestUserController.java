package com.kismet.backend.controller;

import com.kismet.backend.dto.GuestResponse;
import com.kismet.backend.service.GuestUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/guests")
@RequiredArgsConstructor
public class GuestUserController {

    private final GuestUserService guestUserService;

    @PostMapping
    public GuestResponse createGuestUser() {
        return guestUserService.createGuestUser();
    }

    @GetMapping("/{guestId}")
    public GuestResponse getGuestByGuestId(@PathVariable String guestId) {
        return guestUserService.getGuestByGuestId(guestId);
    }
}
