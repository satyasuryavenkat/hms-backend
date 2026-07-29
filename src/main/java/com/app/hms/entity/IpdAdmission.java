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
public class IpdAdmission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String ipdNumber;

  @ManyToOne(optional = false)
  private Patient patient;

  @Enumerated(EnumType.STRING)
  private AdmissionType admissionType;

  private LocalDateTime admissionDateTime;

  @ManyToOne(optional = false)
  private Doctor consultant;

  private String ward;

  @Column(unique = true)
  private String bedNumber;

  private String provisionalDiagnosis;
  private String attendantName;
  private String attendantMobile;

  @Enumerated(EnumType.STRING)
  private AdmissionStatus status = AdmissionStatus.ACTIVE;

  private BigDecimal discount = BigDecimal.ZERO;
  private String discountReason;

  @Enumerated(EnumType.STRING)
  private PaymentMode finalPaymentMode;

  private BigDecimal finalPaidAmount = BigDecimal.ZERO;
  private String paymentReference;
  private OffsetDateTime settledAt;

  @Enumerated(EnumType.STRING)
  private DischargeType dischargeType;

  private OffsetDateTime dischargeDateTime;

  @Column(length = 2000)
  private String finalDiagnosis;

  @Column(length = 4000)
  private String clinicalSummary;

  @Column(length = 2000)
  private String advice;

  private LocalDate followUpDate;
  @ManyToOne private Doctor followUpDoctor;
  @ElementCollection private List<Medication> medications = new ArrayList<>();
  private boolean dischargeDraft;
}
