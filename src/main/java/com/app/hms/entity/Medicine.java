package com.app.hms.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "pharmacy_medicines")
@Getter
@Setter
@NoArgsConstructor
public class Medicine {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String manufacturerCode;

  @Column(nullable = false)
  private String name;

  private String genericName;
  private String type;
  private String manufacturerName;
  private String batchNumber;

  @Column(length = 2000)
  private String description;

  @Column(columnDefinition = "TEXT")
  private String imageData;

  @Column(nullable = false)
  private Integer quantity = 0;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal unitPrice = BigDecimal.ZERO;

  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal taxPercent = BigDecimal.ZERO;

  @Column(nullable = false)
  private LocalDate expiryDate;

  private boolean active = true;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  @PrePersist
  void create() {
    createdAt = OffsetDateTime.now();
    updatedAt = createdAt;
  }

  @PreUpdate
  void update() {
    updatedAt = OffsetDateTime.now();
  }
}
