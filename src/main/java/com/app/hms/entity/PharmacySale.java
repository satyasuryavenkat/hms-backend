package com.app.hms.entity;

import com.app.hms.common.Enums.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.*;

@Entity
@Table(name = "pharmacy_sales")
@Getter
@Setter
@NoArgsConstructor
public class PharmacySale {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String invoiceNumber;

  @ManyToOne private Patient patient;
  private String customerName;
  private String customerMobile;

  @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PharmacySaleItem> items = new ArrayList<>();

  private BigDecimal subtotal = BigDecimal.ZERO;
  private BigDecimal taxAmount = BigDecimal.ZERO;
  private BigDecimal discount = BigDecimal.ZERO;
  private BigDecimal totalPayable = BigDecimal.ZERO;
  private BigDecimal paidAmount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  private PaymentMode paymentMode;

  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus = PaymentStatus.PAID;

  private OffsetDateTime createdAt;

  @PrePersist
  void create() {
    createdAt = OffsetDateTime.now();
  }
}
