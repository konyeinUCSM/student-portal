package com.manulife.studentportal.filter;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String username;
    private final Long userId;
    private final String role;
    private final Long profileId;

    public JwtAuthenticationToken(String username, Long userId, String role, Long profileId,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.username = username;
        this.userId = userId;
        this.role = role;
        this.profileId = profileId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getPrincipal() { return username; }
}