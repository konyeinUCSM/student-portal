package com.manulife.studentportal.security;

import com.manulife.studentportal.dto.response.LoginResponse;
import com.manulife.studentportal.filter.JwtAuthenticationToken;
import com.manulife.studentportal.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {

    private final TeacherRepository teacherRepository;

    public JwtAuthenticationToken getCurrentAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth;
        }
        throw new IllegalStateException("No JWT authentication found in SecurityContext");
    }

    public String getCurrentToken() {
        return getCurrentAuth().getToken();
    }

    public String getCurrentTokenId() {
        return getCurrentAuth().getTokenId();
    }

    public String getCurrentUsername() {
        return getCurrentAuth().getUsername();
    }

    public Long getCurrentUserId() {
        return getCurrentAuth().getUserId();
    }

    public String getCurrentRole() {
        return getCurrentAuth().getRole();
    }

    public Long getCurrentProfileId() {
        return getCurrentAuth().getProfileId();
    }

    public LoginResponse.UserSummary getCurrentUserSummary() {
        JwtAuthenticationToken auth = getCurrentAuth();
        return LoginResponse.UserSummary.builder()
                .id(auth.getUserId())
                .username(auth.getUsername())
                .role(auth.getRole())
                .profileId(auth.getProfileId())
                .build();
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getCurrentRole());
    }

    public boolean isTeacher() {
        return "TEACHER".equals(getCurrentRole());
    }

    public boolean isStudent() {
        return "STUDENT".equals(getCurrentRole());
    }

    public boolean isTeacherOwner(Long teacherId) {
        return teacherId.equals(getCurrentProfileId());
    }

    public boolean isStudentOwner(Long studentId) {
        return studentId.equals(getCurrentProfileId());
    }

    public boolean isTeacherAssignedToClass(Long classId) {
        Long profileId = getCurrentProfileId();
        if (profileId == null) return false;
        return teacherRepository.existsByIdAndClasses_Id(profileId, classId);
    }

    public boolean isTeacherAssignedToSubject(Long subjectId) {
        Long profileId = getCurrentProfileId();
        if (profileId == null) return false;
        return teacherRepository.existsByIdAndSubjects_Id(profileId, subjectId);
    }
}