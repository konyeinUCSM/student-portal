package com.manulife.studentportal.user.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.user.User;
import com.manulife.studentportal.user.UserRepository;
import com.manulife.studentportal.user.Role;
import com.manulife.studentportal.user.UserService;
import com.manulife.studentportal.user.web.CreateUserRequest;
import com.manulife.studentportal.user.web.UpdateUserRequest;
import com.manulife.studentportal.user.web.UserResponse;
import com.manulife.studentportal.shared.exception.DuplicateResourceException;
import com.manulife.studentportal.shared.exception.InvalidOperationException;
import com.manulife.studentportal.shared.exception.ResourceNotFoundException;
import com.manulife.studentportal.auth.LoginSessionService;
import com.manulife.studentportal.academic.AcademicQueryService;
import com.manulife.studentportal.student.StudentRepository;
import com.manulife.studentportal.teacher.TeacherRepository;
import com.manulife.studentportal.shared.security.SecurityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final AcademicQueryService academicQueryService;
    private final LoginSessionService loginSessionService;
    private final SecurityService securityService;

    @Override
    public UserResponse create(CreateUserRequest request) {

        if (userRepository.countByUsernameAllRecords(request.getUsername()) > 0) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        if (request.getEmail() != null && userRepository.countByEmailAllRecords(request.getEmail()) > 0) {
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

        if (request.getEmail() != null) {
            if (userRepository.countByEmailExcludingIdAllRecords(request.getEmail(), id) > 0) {
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

        String deletedBy = securityService.getCurrentUsername();

        // softDelete() override in User enforces active=false atomically
        user.softDelete(deletedBy);

        teacherRepository.findByUserId(id).ifPresent(teacher -> {
            teacher.softDelete(deletedBy);
            log.info("Soft-deleted associated Teacher profile: teacherId={}", teacher.getId());
        });

        studentRepository.findByUserId(id).ifPresent(student -> {
            if (academicQueryService.existsMarkForStudent(student.getId())) {
                throw new InvalidOperationException(
                        "Cannot delete user id " + id + ": associated student has academic mark records. Delete the marks first.");
            }
            student.softDelete(deletedBy);
            log.info("Soft-deleted associated Student profile: studentId={}", student.getId());
        });

        // Terminate all active sessions for this user
        loginSessionService.terminateAllUserSessions(id);

        log.info("User soft deleted successfully with id: {}", id);
    }
}