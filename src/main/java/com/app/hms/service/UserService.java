package com.app.hms.service;

import com.app.hms.dto.request.CreateUserRequest;
import com.app.hms.dto.request.UpdateUserRequest;
import com.app.hms.dto.response.UserResponse;
import java.util.List;

public interface UserService {
  List<UserResponse> findAll();

  UserResponse findById(Long id);

  UserResponse create(CreateUserRequest request);

  UserResponse update(Long id, UpdateUserRequest request);
}
