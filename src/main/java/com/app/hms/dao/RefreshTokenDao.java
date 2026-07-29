package com.app.hms.dao;

import com.app.hms.entity.RefreshToken;
import java.util.Optional;

public interface RefreshTokenDao {
  Optional<RefreshToken> findById(String id);

  RefreshToken save(RefreshToken token);
}
