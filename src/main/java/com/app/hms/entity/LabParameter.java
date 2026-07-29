package com.app.hms.entity;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class LabParameter {
  private Long parameterId;
  private String name;
  private String unit;
  private String referenceRange;
}
