package com.app.hms.entity;

import com.app.hms.common.Enums.LabParameterType;
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

  @Enumerated(EnumType.STRING)
  private LabParameterType parameterType = LabParameterType.NUMERIC;

  private Integer displayOrder;
}
