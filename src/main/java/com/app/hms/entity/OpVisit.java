package com.app.hms.entity;

import com.app.hms.common.Enums.*;
import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OpVisit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String opNumber;

  @Column(unique = true)
  private String receiptNumber;

  @ManyToOne(optional = false)
  private Patient patient;

  @ManyToOne(optional = false)
  private Doctor doctor;

  @Enumerated(EnumType.STRING)
  private VisitType visitType;

  private LocalDate appointmentDate;
  private LocalTime appointmentTime;

  @Column(length = 1000)
  private String symptoms;

  private BigDecimal consultationFee;
  private BigDecimal discount;
  private BigDecimal totalPayable;
  private BigDecimal paidAmount;

  @Enumerated(EnumType.STRING)
  private PaymentMode paymentMode;

  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  private OffsetDateTime createdAt;

  @PrePersist
  void pre() {
    createdAt = OffsetDateTime.now();
  }
}
