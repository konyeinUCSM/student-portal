package com.manulife.studentportal.student;

/**
 * Minimal student information for cross-module queries.
 * Avoids exposing the full Student entity.
 */
public record StudentInfo(Long id, String name, Long classId) {
}
