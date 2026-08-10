package com.app.hms.entity;

import com.app.hms.common.Enums.SpecimenType;
import jakarta.persistence.*;
import java.math.*;
import java.util.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class LabOrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  private LabOrder order;

  private Long testId;
  private String code;
  private String name;
  private String department;

  @Enumerated(EnumType.STRING)
  private SpecimenType specimenType;

  private BigDecimal amount;
  private String status = "ORDERED";

  @Column(columnDefinition = "TEXT")
  private String reportTemplateHtml;

  @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<LabResult> results = new ArrayList<>();
}
