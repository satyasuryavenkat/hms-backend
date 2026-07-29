package com.app.hms.service;

import com.app.hms.dto.request.*;
import com.app.hms.dto.response.*;

public interface AuthService {
  AuthResponse login(LoginRequest request);

  AuthResponse refresh(RefreshTokenRequest request);

  UserResponse currentUser(String username);

  void logout(RefreshTokenRequest request);
}
