package com.app.hms.entity;

import com.app.hms.common.Enums.PaymentMode;
import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class IpdAdvance {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  private IpdAdmission admission;

  @Column(unique = true)
  private String receiptNumber;

  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  private PaymentMode paymentMode;

  private String referenceNumber;
  private String remarks;
  private OffsetDateTime createdAt;

  @PrePersist
  void pre() {
    createdAt = OffsetDateTime.now();
  }
}
