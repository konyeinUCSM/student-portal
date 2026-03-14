package com.manulife.studentportal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.dto.response.DashboardStatsResponse;
import com.manulife.studentportal.dto.response.LoginSessionResponse;

public interface LoginSessionService {

    Page<LoginSessionResponse> getAllSessions(Pageable pageable, Long userId, Boolean active);

    void terminateSession(Long sessionId);

    DashboardStatsResponse getDashboardStats();
}
