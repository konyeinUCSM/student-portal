package com.manulife.studentportal.academic;

/**
 * Data transfer record for school class information.
 * Used to share class data across module boundaries without exposing the entity.
 */
public record SchoolClassInfo(
    Long id,
    String name
) {}
