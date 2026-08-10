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
public class LabTest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String code;

  @Column(nullable = false)
  private String name;

  private String department;
  private BigDecimal price;
  private Integer turnaroundHours;

  @Enumerated(EnumType.STRING)
  private SpecimenType specimenType = SpecimenType.BLOOD;

  private boolean active = true;

  @Column(columnDefinition = "TEXT")
  private String reportTemplateHtml;

  @ElementCollection
  @CollectionTable(name = "lab_test_parameters")
  private List<LabParameter> parameters = new ArrayList<>();
}
