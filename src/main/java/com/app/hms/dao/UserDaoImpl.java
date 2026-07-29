package com.app.hms.dao;

import com.app.hms.entity.AppUser;
import com.app.hms.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {
  private final UserRepository repository;

  public Optional<AppUser> findById(Long id) {
    return repository.findById(id);
  }

  public Optional<AppUser> findByUsername(String username) {
    return repository.findByUsernameIgnoreCase(username);
  }

  public AppUser save(AppUser u) {
    return repository.save(u);
  }

  public List<AppUser> findAll() {
    return repository.findAll();
  }

  public boolean existsByUsername(String username, Long excludedId) {
    return excludedId == null
        ? repository.existsByUsernameIgnoreCase(username)
        : repository.existsByUsernameIgnoreCaseAndIdNot(username, excludedId);
  }

  public long count() {
    return repository.count();
  }
}
