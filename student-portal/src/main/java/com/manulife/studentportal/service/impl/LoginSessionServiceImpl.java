package com.manulife.studentportal.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.dto.response.DashboardStatsResponse;
import com.manulife.studentportal.dto.response.LoginSessionResponse;
import com.manulife.studentportal.entity.LoginSession;
import com.manulife.studentportal.enums.Role;
import com.manulife.studentportal.exception.ResourceNotFoundException;
import com.manulife.studentportal.repository.ExamRepository;
import com.manulife.studentportal.repository.LoginSessionRepository;
import com.manulife.studentportal.repository.SchoolClassRepository;
import com.manulife.studentportal.repository.StudentRepository;
import com.manulife.studentportal.repository.SubjectRepository;
import com.manulife.studentportal.repository.TeacherRepository;
import com.manulife.studentportal.repository.UserRepository;
import com.manulife.studentportal.service.LoginSessionService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        return loginSessionRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            query.orderBy(cb.desc(root.get("loginTime")));

            return cb.and(predicates.toArray(new Predicate[0]));

        }, pageable).map(this::toResponse);
    }

    @Override
    public void terminateSession(Long sessionId) {
        log.info("Terminating session with id: {}", sessionId);

        LoginSession session = loginSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        session.setActive(false);

        log.info("Session terminated successfully: sessionId={}, userId={}", sessionId, session.getUser().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        log.debug("Calculating dashboard statistics");

        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalAdmins(userRepository.countByRole(Role.ADMIN))
                .totalTeachers(teacherRepository.count())
                .totalStudents(studentRepository.count())
                .totalClasses(schoolClassRepository.count())
                .totalSubjects(subjectRepository.count())
                .totalExams(examRepository.count())
                .activeSessions(loginSessionRepository.countByActiveTrue())
                .build();
    }

    private LoginSessionResponse toResponse(LoginSession session) {
        var user = session.getUser();
        return LoginSessionResponse.builder()
                .id(session.getId())
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getUsername() : null)
                .role(user != null ? user.getRole().name() : null)
                .loginTime(session.getLoginTime())
                .expiryTime(session.getExpiryTime())
                .ipAddress(session.getIpAddress())
                .active(session.isActive())
                .build();
    }
}