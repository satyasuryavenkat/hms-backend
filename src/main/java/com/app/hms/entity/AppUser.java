package com.app.hms.entity;

import com.app.hms.common.Enums.UserRole;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_permissions")
  private Set<String> permissions = new HashSet<>();

  private boolean active = true;
}
