package com.kismet.backend.controller;

import com.kismet.backend.dto.ReportRequest;
import com.kismet.backend.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.security.Principal;

@RestController
@CrossOrigin
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public String createReport(Principal principal, @Valid @RequestBody ReportRequest request) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }
        request.setReporterGuestId(principal.getName());
        return reportService.createReport(request);
    }
}
