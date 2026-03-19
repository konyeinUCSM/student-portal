package com.manulife.studentportal.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.dto.request.ChangePasswordRequest;
import com.manulife.studentportal.dto.request.LoginRequest;
import com.manulife.studentportal.dto.response.LoginResponse;
import com.manulife.studentportal.entity.LoginSession;
import com.manulife.studentportal.entity.User;
import com.manulife.studentportal.shared.exception.BusinessLogicException;
import com.manulife.studentportal.shared.exception.ResourceNotFoundException;
import com.manulife.studentportal.shared.exception.UnauthorizedException;
import com.manulife.studentportal.repository.LoginSessionRepository;
import com.manulife.studentportal.repository.UserRepository;
import com.manulife.studentportal.security.CustomUserDetails;
import com.manulife.studentportal.security.JwtTokenProvider;
import com.manulife.studentportal.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest loginRequest, String ipAddress) {
        log.info("Login attempt for username: {}", loginRequest.getUsername());

        try {
            // Authenticate via Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Get authenticated user details
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = customUserDetails.getUser();
            Long userId = user.getId();
            String role = user.getRole().name();
            Long profileId = customUserDetails.getProfileId();

            log.info("Login successful for userId: {}, role: {}", userId, role);

            // Generate JWT with jti
            String jti = UUID.randomUUID().toString();
            String token = jwtTokenProvider.generateToken(
                loginRequest.getUsername(),
                userId,
                role,
                profileId,
                jti
            );

            // Create LoginSession in DB
            LoginSession loginSession = new LoginSession();
            loginSession.setTokenId(jti);
            loginSession.setUser(user);
            loginSession.setLoginTime(LocalDateTime.now());
            loginSession.setExpiryTime(LocalDateTime.now().plusSeconds(jwtTokenProvider.getExpirationInSeconds()));
            loginSession.setIpAddress(ipAddress);
            loginSession.setActive(true);
            loginSessionRepository.save(loginSession);

            // Build response
            LoginResponse.UserSummary userSummary = LoginResponse.UserSummary.builder()
                .id(userId)
                .username(loginRequest.getUsername())
                .role(role)
                .profileId(profileId)
                .build();

            return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationInSeconds())
                .user(userSummary)
                .build();

        } catch (BadCredentialsException | UsernameNotFoundException e) {
            log.warn("Failed login attempt for username: {}", loginRequest.getUsername());
            throw new UnauthorizedException("Invalid username or password");
        } catch (Exception e) {
            log.error("Unexpected error during login for username: {}", loginRequest.getUsername(), e);
            throw new RuntimeException("An error occurred during login. Please try again later.", e);
        }
    }

    @Override
    public void logout(String tokenId) {
        // Find LoginSession by tokenId and set active = false
        LoginSession session = loginSessionRepository.findByTokenId(tokenId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        session.setActive(false);

        log.info("Logout successful for session: {}", tokenId);
    }

    @Override
    public void changePassword(ChangePasswordRequest request, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessLogicException("Old password is incorrect");
        }

        // Encode and set new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        log.info("Password changed successfully for userId: {}", userId);
    }
}