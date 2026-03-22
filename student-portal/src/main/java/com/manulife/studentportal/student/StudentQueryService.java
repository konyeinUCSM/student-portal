package com.manulife.studentportal.student;

import java.util.Optional;

/**
 * Public query service for cross-module student queries.
 * Exposes read-only operations needed by other modules.
 */
public interface StudentQueryService {

    /**
     * Find student by ID, returns minimal info to avoid entity coupling
     */
    Optional<StudentInfo> findById(Long id);

    /**
     * Check if any students are enrolled in a class
     */
    boolean existsByClassId(Long classId);
}
