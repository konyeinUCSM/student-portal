package com.manulife.studentportal.auth;

public class SecurityExpressions {

    // Complex expressions for fine-grained access control
    public static final String ADMIN_OR_OWNER_STUDENT =
        "hasRole('ADMIN') or hasRole('TEACHER') or (hasRole('STUDENT') and @securityService.isStudentOwner(#id))";

    public static final String ADMIN_OR_OWNER_TEACHER =
        "hasRole('ADMIN') or (hasRole('TEACHER') and @securityService.isTeacherOwner(#id))";

    public static final String ADMIN_OR_ASSIGNED_TO_CLASS =
        "hasRole('ADMIN') or (hasRole('TEACHER') and @securityService.isTeacherAssignedToClass(#id))";

    public static final String ADMIN_OR_ASSIGNED_TO_SUBJECT =
        "hasRole('ADMIN') or (hasRole('TEACHER') and @securityService.isTeacherAssignedToSubject(#id))";

    private SecurityExpressions() {
        // Utility class
    }
}
