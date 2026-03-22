package com.manulife.studentportal.auth;

import com.manulife.studentportal.auth.web.ChangePasswordRequest;
import com.manulife.studentportal.auth.web.LoginRequest;
import com.manulife.studentportal.auth.web.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest, String ipAddress);

    void logout(String tokenId);

    void changePassword(ChangePasswordRequest request, Long userId);
}