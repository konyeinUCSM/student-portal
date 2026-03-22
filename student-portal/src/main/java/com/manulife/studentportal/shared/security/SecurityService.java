package com.manulife.studentportal.shared.security;

import com.manulife.studentportal.auth.web.LoginResponse;

/**
 * Security service for accessing current authentication context.
 * Provides methods to retrieve information about the currently authenticated user.
 */
public interface SecurityService {

    /**
     * Get the current token ID.
     * @return the JWT token ID (jti claim)
     */
    String getCurrentTokenId();

    /**
     * Get the current authenticated username.
     * @return the username
     */
    String getCurrentUsername();

    /**
     * Get the current authenticated user's ID.
     * @return the user ID
     */
    Long getCurrentUserId();

    /**
     * Get the current authenticated user's role.
     * @return the role (ADMIN, TEACHER, STUDENT)
     */
    String getCurrentRole();

    /**
     * Get the current authenticated user's profile ID.
     * For teachers, this is the teacher ID. For students, this is the student ID.
     * @return the profile ID
     */
    Long getCurrentProfileId();

    /**
     * Get a summary of the current authenticated user.
     * @return user summary containing id, username, role, and profile ID
     */
    LoginResponse.UserSummary getCurrentUserSummary();

    /**
     * Check if the current user is an admin.
     * @return true if the current user has ADMIN role
     */
    boolean isAdmin();

    /**
     * Check if the current user is a teacher.
     * @return true if the current user has TEACHER role
     */
    boolean isTeacher();

    /**
     * Check if the current user is a student.
     * @return true if the current user has STUDENT role
     */
    boolean isStudent();

    /**
     * Check if the current teacher is the owner of the given teacher profile.
     * @param teacherId the teacher ID to check
     * @return true if the current user's profile ID matches the given teacher ID
     */
    boolean isTeacherOwner(Long teacherId);

    /**
     * Check if the current student is the owner of the given student profile.
     * @param studentId the student ID to check
     * @return true if the current user's profile ID matches the given student ID
     */
    boolean isStudentOwner(Long studentId);

    /**
     * Check if the current teacher is assigned to the given class.
     * @param classId the class ID to check
     * @return true if the teacher is assigned to the class
     */
    boolean isTeacherAssignedToClass(Long classId);

    /**
     * Check if the current teacher is assigned to the given subject.
     * @param subjectId the subject ID to check
     * @return true if the teacher is assigned to the subject
     */
    boolean isTeacherAssignedToSubject(Long subjectId);
}
