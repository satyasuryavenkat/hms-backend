package com.app.hms.entity;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Medication {
  private String medicine;
  private String dose;
  private String frequency;
  private String duration;
  private String instructions;
}
