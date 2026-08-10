package com.app.hms.entity;

import com.app.hms.common.Enums.FollowUpStatus;
import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Entity
@Table(
    name = "patient_follow_ups",
    indexes = {
      @Index(name = "idx_follow_up_visit_time", columnList = "visitDateTime"),
      @Index(name = "idx_follow_up_reminder", columnList = "reminderVisible,status")
    })
@Getter
@Setter
@NoArgsConstructor
public class FollowUp {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Patient patient;

  @Column(nullable = false)
  private LocalDateTime visitDateTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FollowUpStatus status = FollowUpStatus.PENDING;

  @Column(nullable = false)
  private boolean reminderVisible;

  private OffsetDateTime reminderActivatedAt;

  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false, updatable = false)
  private String createdBy;

  private OffsetDateTime remindedAt;
  private String remindedBy;

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = OffsetDateTime.now(ZoneId.of("Asia/Kolkata"));
  }
}
