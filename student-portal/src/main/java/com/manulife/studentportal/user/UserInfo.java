package com.manulife.studentportal.user;

/**
 * Minimal user information DTO for cross-module queries.
 * Avoids exposing the User entity outside the user module.
 */
public record UserInfo(
    Long id,
    String username,
    String email,
    Role role,
    Boolean active
) {
}
