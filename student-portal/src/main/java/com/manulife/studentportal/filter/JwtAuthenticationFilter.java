package com.manulife.studentportal.filter;

import java.io.IOException;
import java.util.List;

import org.slf4j.MDC;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.manulife.studentportal.repository.LoginSessionRepository;
import com.manulife.studentportal.security.JwtTokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final LoginSessionRepository loginSessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            String tokenId = jwtTokenProvider.getTokenId(token);

            // Check session is still active in DB
            boolean sessionActive = loginSessionRepository.existsByTokenIdAndActiveTrue(tokenId);
            if (sessionActive) {
                String username = jwtTokenProvider.getUsername(token);
                Long userId = jwtTokenProvider.getUserId(token);
                String role = jwtTokenProvider.getRole(token);
                Long profileId = jwtTokenProvider.getProfileId(token);

                // Set userId in MDC for logging
                MDC.put("userId", String.valueOf(userId));

                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + role));

                JwtAuthenticationToken authToken = new JwtAuthenticationToken(
                        username, userId, role, profileId, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                log.warn("Session invalidated for tokenId: {}", tokenId);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}