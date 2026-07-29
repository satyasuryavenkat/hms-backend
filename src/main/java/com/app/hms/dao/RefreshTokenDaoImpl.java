package com.app.hms.dao;

import com.app.hms.entity.RefreshToken;
import com.app.hms.repository.RefreshTokenRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenDaoImpl implements RefreshTokenDao {
  private final RefreshTokenRepository repository;

  public Optional<RefreshToken> findById(String id) {
    return repository.findById(id);
  }

  public RefreshToken save(RefreshToken t) {
    return repository.save(t);
  }
}
