package com.app.hms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "ipd_charge_catalog")
@Getter
@Setter
@NoArgsConstructor
public class IpdChargeCatalogItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 80)
  private String category;

  @Column(nullable = false, length = 120)
  private String categoryLabel;

  @Column(nullable = false, unique = true, length = 80)
  private String serviceCode;

  @Column(nullable = false, length = 180)
  private String serviceName;

  @Column(nullable = false, length = 140)
  private String department;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal defaultRate = BigDecimal.ZERO;

  @Column(nullable = false)
  private boolean active = true;
}
