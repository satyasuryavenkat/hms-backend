package com.app.hms.entity;

import com.app.hms.common.Enums.*;
import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class LabOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String orderNumber;

  @ManyToOne(optional = false)
  private Patient patient;

  @ManyToOne private Doctor referringDoctor;

  @Enumerated(EnumType.STRING)
  private LabPriority priority;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<LabOrderItem> tests = new ArrayList<>();

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<LabSpecimen> specimens = new ArrayList<>();

  private BigDecimal subtotal = BigDecimal.ZERO;
  private BigDecimal discount = BigDecimal.ZERO;
  private BigDecimal totalPayable = BigDecimal.ZERO;
  private BigDecimal paidAmount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  private PaymentMode paymentMode;

  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @Enumerated(EnumType.STRING)
  private LabReportStatus reportStatus = LabReportStatus.PENDING;

  private String cancellationReason;
  private String remarks;
  private OffsetDateTime createdAt;
  private OffsetDateTime verifiedAt;
  private Long verifiedBy;

  @PrePersist
  void pre() {
    createdAt = OffsetDateTime.now();
  }
}
