package com.kismet.backend.dto;

import com.kismet.backend.enums.ReportReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import javax.management.remote.JMXServerErrorException;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {

    @NotBlank(message = "Room Id is required")
    private String roomId;

    @NotBlank(message = "Reporter guest id is required")
    private  String reporterGuestId;
    @NotBlank(message = "Reported guest id is required")
    private  String reportedGuestId;

    @NotNull(message = "Report reason is required")
    private ReportReason reason;

    private String description;
}
