package com.kismet.backend.entity;


import com.kismet.backend.enums.ReportReason;
import com.kismet.backend.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.apachecommons.CommonsLog;

import java.time.LocalDateTime;

@Entity
@Table(name="chat_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private String reporterGuestId;
    @Column(nullable = false)
    private String reportedGuestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private ReportStatus status;

    private LocalDateTime createdAt;
}
