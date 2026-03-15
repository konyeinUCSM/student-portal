package com.manulife.studentportal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.dto.request.CreateUserRequest;
import com.manulife.studentportal.dto.request.UpdateUserRequest;
import com.manulife.studentportal.dto.response.UserResponse;
import com.manulife.studentportal.enums.Role;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    UserResponse getById(Long id);

    Page<UserResponse> getAll(Pageable pageable, Role role);

    UserResponse update(Long id, UpdateUserRequest request);

    void delete(Long id);
}