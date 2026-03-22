package com.manulife.studentportal.teacher;

import java.util.Optional;

/**
 * Public query service for cross-module teacher queries.
 * Exposes read-only operations needed by other modules.
 */
public interface TeacherQueryService {

    /**
     * Find teacher by ID, returns minimal info to avoid entity coupling
     */
    Optional<TeacherInfo> findById(Long id);

    /**
     * Check if teacher is assigned to a specific class
     */
    boolean isTeacherAssignedToClass(Long teacherId, Long classId);
}
