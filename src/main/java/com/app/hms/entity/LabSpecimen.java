package com.app.hms.entity;

import com.app.hms.common.Enums.SpecimenType;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "lab_specimens")
@Getter
@Setter
@NoArgsConstructor
public class LabSpecimen {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  private LabOrder order;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SpecimenType specimenType;

  @Column(nullable = false, unique = true)
  private String barcode;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  void create() {
    createdAt = OffsetDateTime.now();
  }
}
