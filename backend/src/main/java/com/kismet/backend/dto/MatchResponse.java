package com.kismet.backend.dto;

import com.kismet.backend.enums.MatchStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponse {

    private MatchStatus status;

    private String message;

    private String roomId;

    private String guestId;

    private String strangerGuestId;
}