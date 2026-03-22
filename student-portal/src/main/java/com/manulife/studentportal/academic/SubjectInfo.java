package com.manulife.studentportal.academic;

/**
 * Data transfer record for subject information.
 * Used to share subject data across module boundaries without exposing the entity.
 */
public record SubjectInfo(
    Long id,
    String name
) {}
