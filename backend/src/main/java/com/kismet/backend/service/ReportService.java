package com.kismet.backend.service;

import com.kismet.backend.dto.ReportRequest;
import com.kismet.backend.entity.ChatReport;
import com.kismet.backend.enums.ReportStatus;
import com.kismet.backend.repository.ChatReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ChatReportRepository chatReportRepository;

    public String createReport(ReportRequest request) {

        ChatReport report = ChatReport.builder()
                .roomId(request.getRoomId())
                .reporterGuestId(request.getReporterGuestId())
                .reportedGuestId(request.getReportedGuestId())
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        chatReportRepository.save(report);

        return "Report submitted successfully";
    }
}
