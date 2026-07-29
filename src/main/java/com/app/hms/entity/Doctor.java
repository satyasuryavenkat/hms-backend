package com.app.hms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
public class Doctor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String doctorCode;

  @Column(nullable = false)
  private String name;

  private String department;
  private String specialization;

  @Column(precision = 12, scale = 2)
  private BigDecimal consultationFee;

  private boolean active = true;
}
