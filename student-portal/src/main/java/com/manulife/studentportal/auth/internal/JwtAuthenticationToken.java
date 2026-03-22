package com.manulife.studentportal.auth.internal;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;
    private final String tokenId;
    private final String username;
    private final Long userId;
    private final String role;
    private final Long profileId;

    public JwtAuthenticationToken(String token, String tokenId, String username, Long userId, String role,
            Long profileId,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.tokenId = tokenId;
        this.username = username;
        this.userId = userId;
        this.role = role;
        this.profileId = profileId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }
}