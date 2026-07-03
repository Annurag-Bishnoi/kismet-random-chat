package com.kismet.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatLeaveRequest {
    private String guestId;
    private String roomId;
}
