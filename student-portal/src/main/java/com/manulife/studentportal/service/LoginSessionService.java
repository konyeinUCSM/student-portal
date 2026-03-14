package com.manulife.studentportal.service;

import com.manulife.studentportal.dto.response.DashboardStatsResponse;
import com.manulife.studentportal.dto.response.LoginSessionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoginSessionService {

    Page<LoginSessionResponse> getAllSessions(Pageable pageable, Long userId, Boolean active);

    Page<LoginSessionResponse> getActiveSessions(Pageable pageable);

    void terminateSession(Long sessionId);

    DashboardStatsResponse getDashboardStats();
}
