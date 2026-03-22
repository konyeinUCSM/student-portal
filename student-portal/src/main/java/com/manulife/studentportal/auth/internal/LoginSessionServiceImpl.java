package com.manulife.studentportal.auth.internal;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.auth.LoginSessionService;
import com.manulife.studentportal.shared.dto.DashboardStatsResponse;
import com.manulife.studentportal.auth.web.LoginSessionResponse;
import com.manulife.studentportal.user.Role;
import com.manulife.studentportal.shared.exception.ResourceNotFoundException;
import com.manulife.studentportal.academic.AcademicQueryService;
import com.manulife.studentportal.student.StudentRepository;
import com.manulife.studentportal.teacher.TeacherRepository;
import com.manulife.studentportal.user.UserRepository;

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
    private final AcademicQueryService academicQueryService;

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
    public void terminateAllUserSessions(Long userId) {
        log.info("Terminating all active sessions for userId: {}", userId);

        List<LoginSession> activeSessions = loginSessionRepository.findAll((root, query, cb) ->
            cb.and(
                cb.equal(root.get("user").get("id"), userId),
                cb.equal(root.get("active"), true)
            )
        );

        int terminatedCount = 0;
        for (LoginSession session : activeSessions) {
            session.setActive(false);
            terminatedCount++;
            log.debug("Terminated session: sessionId={}, tokenId={}", session.getId(), session.getTokenId());
        }

        log.info("Terminated {} active session(s) for userId: {}", terminatedCount, userId);
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
                .totalClasses(academicQueryService.countClasses())
                .totalSubjects(academicQueryService.countSubjects())
                .totalExams(academicQueryService.countExams())
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