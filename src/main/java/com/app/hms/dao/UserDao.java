package com.app.hms.dao;

import com.app.hms.entity.AppUser;
import java.util.List;
import java.util.Optional;

public interface UserDao {
  Optional<AppUser> findById(Long id);

  Optional<AppUser> findByUsername(String username);

  AppUser save(AppUser user);

  List<AppUser> findAll();

  boolean existsByUsername(String username, Long excludedId);

  long count();
}
