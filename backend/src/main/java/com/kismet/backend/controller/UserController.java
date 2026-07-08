package com.kismet.backend.controller;

import com.kismet.backend.entity.User;
import com.kismet.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public User getMyProfile(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }
        return userService.getUserByEmail(principal.getName());
    }
}
