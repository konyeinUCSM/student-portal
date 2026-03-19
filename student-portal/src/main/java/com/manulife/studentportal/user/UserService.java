package com.manulife.studentportal.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.user.web.CreateUserRequest;
import com.manulife.studentportal.user.web.UpdateUserRequest;
import com.manulife.studentportal.user.web.UserResponse;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    UserResponse getById(Long id);

    Page<UserResponse> getAll(Pageable pageable, Role role);

    UserResponse update(Long id, UpdateUserRequest request);

    void delete(Long id);
}