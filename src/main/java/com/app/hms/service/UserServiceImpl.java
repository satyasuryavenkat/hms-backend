package com.app.hms.service;

import com.app.hms.common.BadRequestException;
import com.app.hms.common.Enums.UserRole;
import com.app.hms.common.NotFoundException;
import com.app.hms.dao.UserDao;
import com.app.hms.dto.request.CreateUserRequest;
import com.app.hms.dto.request.UpdateUserRequest;
import com.app.hms.dto.response.UserResponse;
import com.app.hms.entity.AppUser;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
  private final UserDao dao;
  private final PasswordEncoder passwordEncoder;

  public List<UserResponse> findAll() {
    return dao.findAll().stream()
        .sorted(Comparator.comparing(AppUser::getDisplayName, String.CASE_INSENSITIVE_ORDER))
        .map(this::map)
        .toList();
  }

  public UserResponse findById(Long id) {
    return map(entity(id));
  }

  @Transactional
  public UserResponse create(CreateUserRequest request) {
    if (dao.existsByUsername(request.username(), null)) {
      throw new BadRequestException("Username already exists");
    }
    AppUser user = new AppUser();
    apply(
        user,
        request.username(),
        request.displayName(),
        request.role(),
        request.active(),
        request.permissions());
    user.setPassword(passwordEncoder.encode(request.password()));
    return map(dao.save(user));
  }

  @Transactional
  public UserResponse update(Long id, UpdateUserRequest request) {
    if (dao.existsByUsername(request.username(), id)) {
      throw new BadRequestException("Username already exists");
    }
    AppUser user = entity(id);
    apply(
        user,
        request.username(),
        request.displayName(),
        request.role(),
        request.active(),
        request.permissions());
    if (request.password() != null && !request.password().isBlank()) {
      user.setPassword(passwordEncoder.encode(request.password()));
    }
    return map(dao.save(user));
  }

  private AppUser entity(Long id) {
    return dao.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
  }

  private void apply(
      AppUser user,
      String username,
      String displayName,
      UserRole role,
      boolean active,
      Set<String> permissions) {
    user.setUsername(username.trim());
    user.setDisplayName(displayName.trim());
    user.setRole(role);
    user.setActive(active);
    user.setPermissions(permissions == null ? new HashSet<>() : new HashSet<>(permissions));
  }

  private UserResponse map(AppUser user) {
    String initials =
        Arrays.stream(user.getDisplayName().trim().split("\\s+"))
            .filter(part -> !part.isBlank())
            .limit(2)
            .map(part -> part.substring(0, 1).toUpperCase())
            .reduce("", String::concat);
    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getDisplayName(),
        user.getRole(),
        user.isActive(),
        initials,
        user.getPermissions());
  }
}
