package com.manulife.studentportal.academic;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Public query service for cross-module academic queries.
 * Exposes read-only operations needed by other modules.
 */
public interface AcademicQueryService {

    /**
     * Check if any marks exist for a student
     */
    boolean existsMarkForStudent(Long studentId);

    /**
     * Count total classes
     */
    long countClasses();

    /**
     * Count total subjects
     */
    long countSubjects();

    /**
     * Count total exams
     */
    long countExams();

    /**
     * Find class by ID, returns minimal info to avoid entity coupling
     */
    Optional<SchoolClassInfo> findClassById(Long id);

    /**
     * Find subject by ID, returns minimal info to avoid entity coupling
     */
    Optional<SubjectInfo> findSubjectById(Long id);

    /**
     * Find classes by IDs
     */
    List<SchoolClassInfo> findClassesByIds(Set<Long> classIds);

    /**
     * Find subjects by IDs
     */
    List<SubjectInfo> findSubjectsByIds(Set<Long> subjectIds);

    /**
     * Check if class exists
     */
    boolean existsClassById(Long classId);

    /**
     * Check if subject exists
     */
    boolean existsSubjectById(Long subjectId);
}
