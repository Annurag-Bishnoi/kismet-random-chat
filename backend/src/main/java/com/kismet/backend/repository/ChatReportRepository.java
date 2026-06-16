package com.kismet.backend.repository;

import com.kismet.backend.entity.ChatReport;
import com.kismet.backend.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatReportRepository extends JpaRepository<ChatReport,Long> {

    List<ChatReport> findByStatus(ReportStatus status);
    List<ChatReport> findByReportedGuestId(String reportedGuestId);

}
