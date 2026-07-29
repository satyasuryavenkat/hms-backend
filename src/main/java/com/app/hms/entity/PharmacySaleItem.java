package com.app.hms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "pharmacy_sale_items")
@Getter
@Setter
@NoArgsConstructor
public class PharmacySaleItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  private PharmacySale sale;

  private Long medicineId;
  private String manufacturerCode;
  private String medicineName;
  private String type;
  private String batchNumber;
  private LocalDate expiryDate;
  private Integer quantity;
  private BigDecimal unitPrice;
  private BigDecimal taxPercent;
  private BigDecimal lineSubtotal;
  private BigDecimal taxAmount;
  private BigDecimal lineTotal;
}
