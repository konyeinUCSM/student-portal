package com.manulife.studentportal.service.impl;

import com.manulife.studentportal.dto.request.CreateUserRequest;
import com.manulife.studentportal.dto.request.UpdateUserRequest;
import com.manulife.studentportal.dto.response.UserResponse;
import com.manulife.studentportal.entity.User;
import com.manulife.studentportal.enums.Role;
import com.manulife.studentportal.exception.DuplicateResourceException;
import com.manulife.studentportal.exception.ResourceNotFoundException;
import com.manulife.studentportal.mapper.UserMapper;
import com.manulife.studentportal.repository.LoginSessionRepository;
import com.manulife.studentportal.repository.StudentRepository;
import com.manulife.studentportal.repository.TeacherRepository;
import com.manulife.studentportal.repository.UserRepository;
import com.manulife.studentportal.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final LoginSessionRepository loginSessionRepository;

    @Override
    public UserResponse create(CreateUserRequest request) {
        // Validate username unique
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        // Validate email unique if provided
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRole())
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {} and username: {}", savedUser.getId(), savedUser.getUsername());

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable, Role role) {
        Page<User> users;
        if (role != null) {
            users = userRepository.findByRole(role, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(userMapper::toResponse);
    }

    @Override
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update only non-null fields
        if (request.getEmail() != null) {
            // Check if email is already taken by another user
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new DuplicateResourceException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", updatedUser.getId());

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Soft delete user
        user.setDeleted(true);
        user.setActive(false);

        // Cascade soft-delete to Teacher profile if exists
        teacherRepository.findByUserId(id).ifPresent(teacher -> {
            teacher.setDeleted(true);
            log.info("Soft-deleted associated Teacher profile: teacherId={}", teacher.getId());
        });

        // Cascade soft-delete to Student profile if exists
        studentRepository.findByUserId(id).ifPresent(student -> {
            student.setDeleted(true);
            log.info("Soft-deleted associated Student profile: studentId={}", student.getId());
        });

        // Terminate all active sessions for this user
        loginSessionRepository.findAll((root, query, cb) ->
            cb.and(
                cb.equal(root.get("user").get("id"), id),
                cb.equal(root.get("active"), true)
            )
        ).forEach(session -> {
            session.setActive(false);
            log.info("Terminated active session: sessionId={}, tokenId={}", session.getId(), session.getTokenId());
        });

        log.info("User soft deleted successfully with id: {}", id);
    }
}