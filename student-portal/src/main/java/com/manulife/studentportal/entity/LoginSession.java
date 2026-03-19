package com.manulife.studentportal.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.NotFound;

import com.manulife.studentportal.shared.entity.BaseEntity;
import com.manulife.studentportal.user.internal.User;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "login_sessions", indexes = {
        @Index(name = "idx_login_session_token_id", columnList = "tokenId", unique = true),
        @Index(name = "idx_login_session_user_id", columnList = "user_id"),
        @Index(name = "idx_login_session_active", columnList = "active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginSession extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private User user;

    @Column(nullable = false)
    private LocalDateTime loginTime;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @Column(length = 45)
    private String ipAddress;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

}