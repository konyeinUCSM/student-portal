package com.manulife.studentportal.service.impl;

import com.manulife.studentportal.dto.response.DashboardStatsResponse;
import com.manulife.studentportal.dto.response.LoginSessionResponse;
import com.manulife.studentportal.entity.LoginSession;
import com.manulife.studentportal.enums.Role;
import com.manulife.studentportal.exception.ResourceNotFoundException;
import com.manulife.studentportal.repository.*;
import com.manulife.studentportal.service.LoginSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoginSessionServiceImpl implements LoginSessionService {

    private final LoginSessionRepository loginSessionRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final ExamRepository examRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<LoginSessionResponse> getAllSessions(Pageable pageable, Long userId, Boolean active) {
        log.debug("Fetching all sessions with filters - userId: {}, active: {}", userId, active);

        Page<LoginSession> sessions;

        if (userId != null && active != null) {
            sessions = loginSessionRepository.findByUserIdAndActive(userId, active, pageable);
        } else if (userId != null) {
            sessions = loginSessionRepository.findByUserId(userId, pageable);
        } else if (active != null) {
            sessions = loginSessionRepository.findByActive(active, pageable);
        } else {
            sessions = loginSessionRepository.findAllByOrderByLoginTimeDesc(pageable);
        }

        return sessions.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoginSessionResponse> getActiveSessions(Pageable pageable) {
        log.debug("Fetching active sessions");
        Page<LoginSession> sessions = loginSessionRepository.findByActiveTrue(pageable);
        return sessions.map(this::toResponse);
    }

    @Override
    public void terminateSession(Long sessionId) {
        log.info("Terminating session with id: {}", sessionId);

        LoginSession session = loginSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        Long targetUserId = session.getUser().getId();

        // Set session as inactive
        session.setActive(false);
        loginSessionRepository.save(session);

        log.info("Session terminated successfully: sessionId={}, targetUserId={}", sessionId, targetUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        log.debug("Calculating dashboard statistics");

        long totalUsers = userRepository.count();
        long totalAdmins = userRepository.findByRole(Role.ADMIN, Pageable.unpaged()).getTotalElements();
        long totalTeachers = teacherRepository.count();
        long totalStudents = studentRepository.count();
        long totalClasses = schoolClassRepository.count();
        long totalSubjects = subjectRepository.count();
        long totalExams = examRepository.count();
        long activeSessions = loginSessionRepository.countByActiveTrue();

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalAdmins(totalAdmins)
                .totalTeachers(totalTeachers)
                .totalStudents(totalStudents)
                .totalClasses(totalClasses)
                .totalSubjects(totalSubjects)
                .totalExams(totalExams)
                .activeSessions(activeSessions)
                .build();
    }

    private LoginSessionResponse toResponse(LoginSession session) {
        if (session == null) {
            return null;
        }

        return LoginSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUser().getId())
                .username(session.getUser().getUsername())
                .role(session.getUser().getRole().name())
                .loginTime(session.getLoginTime())
                .expiryTime(session.getExpiryTime())
                .ipAddress(session.getIpAddress())
                .active(session.isActive())
                .build();
    }
}
