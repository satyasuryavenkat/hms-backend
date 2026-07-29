package com.app.hms.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {
  @Id private String tokenId;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Instant expiresAt;

  private boolean revoked;
}
