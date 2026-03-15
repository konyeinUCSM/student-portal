package com.manulife.studentportal.entity;

import org.hibernate.annotations.SQLRestriction;

import com.manulife.studentportal.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username", unique = true),
        @Index(name = "idx_user_role", columnList = "role")
})
@SQLRestriction("deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends SoftDeletableEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    /**
     * Two-flag lifecycle:
     *   active=true,  deleted=false → normal, can log in
     *   active=false, deleted=false → suspended, cannot log in (account still exists)
     *   active=false, deleted=true  → permanently soft-deleted (invisible via @SQLRestriction)
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Overrides softDelete to enforce the invariant that a deleted user
     * is always deactivated. Prevents active=true, deleted=true state.
     */
    @Override
    public void softDelete(String username) {
        this.active = false;
        super.softDelete(username);
    }
}