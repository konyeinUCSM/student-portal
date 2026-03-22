package com.manulife.studentportal.auth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.shared.dto.DashboardStatsResponse;
import com.manulife.studentportal.auth.web.LoginSessionResponse;

public interface LoginSessionService {

    Page<LoginSessionResponse> getAllSessions(Pageable pageable, Long userId, Boolean active);

    void terminateSession(Long sessionId);

    /**
     * Terminate all active sessions for a specific user
     */
    void terminateAllUserSessions(Long userId);

    DashboardStatsResponse getDashboardStats();
}
