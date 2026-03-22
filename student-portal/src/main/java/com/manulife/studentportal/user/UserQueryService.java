package com.manulife.studentportal.user;

import java.util.Optional;

/**
 * Public query service for cross-module user queries.
 * Exposes read-only operations needed by other modules.
 */
public interface UserQueryService {

    /**
     * Find user info by ID
     */
    Optional<UserInfo> findById(Long id);

    /**
     * Check if user exists by ID
     */
    boolean existsById(Long id);
}
