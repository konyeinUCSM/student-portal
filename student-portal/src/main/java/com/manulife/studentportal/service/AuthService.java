package com.manulife.studentportal.service;

import com.manulife.studentportal.dto.request.ChangePasswordRequest;
import com.manulife.studentportal.dto.request.LoginRequest;
import com.manulife.studentportal.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest, String ipAddress);

    void logout(String token);

    LoginResponse.UserSummary getCurrentUser(String token);

    void changePassword(ChangePasswordRequest request, Long userId);
}