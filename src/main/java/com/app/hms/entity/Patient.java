package com.app.hms.entity;

import com.app.hms.common.Enums.Gender;
import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
public class Patient {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String uhid;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, length = 15)
  private String mobile;

  @Column(nullable = false)
  private LocalDate dateOfBirth;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Gender gender;

  @Column(length = 500)
  private String address;

  @Column(nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  void onCreate() {
    createdAt = OffsetDateTime.now();
  }

  public int calculateAge() {
    return Period.between(dateOfBirth, LocalDate.now()).getYears();
  }
}
