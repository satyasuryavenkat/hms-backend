package com.app.hms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class LabResult {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  private LabOrderItem item;

  private Long parameterId;
  private String name;
  private String result;
  private String unit;
  private String referenceRange;
  private String remarks;
  private boolean abnormal;
}
