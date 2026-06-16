package com.kismet.backend.dto;

import com.kismet.backend.enums.GuestStatus;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestResponse {

    private String guestId;
    private String displayName;
    private GuestStatus status;
}
