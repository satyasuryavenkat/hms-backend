package com.app.hms.entity;

import jakarta.persistence.*;
import java.math.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class IpdCharge {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  private IpdAdmission admission;

  private String category;
  private String department;
  private String serviceCode;
  private String description;
  private BigDecimal quantity;
  private BigDecimal rate;
  private BigDecimal amount;
}
