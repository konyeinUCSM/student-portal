package com.manulife.studentportal.auth.internal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.manulife.studentportal.auth.web.LoginResponse;
import com.manulife.studentportal.shared.security.SecurityService;
import com.manulife.studentportal.teacher.TeacherRepository;

import lombok.RequiredArgsConstructor;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

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

    @Override
    public String getCurrentTokenId() {
        return getCurrentAuth().getTokenId();
    }

    @Override
    public String getCurrentUsername() {
        return getCurrentAuth().getUsername();
    }

    @Override
    public Long getCurrentUserId() {
        return getCurrentAuth().getUserId();
    }

    @Override
    public String getCurrentRole() {
        return getCurrentAuth().getRole();
    }

    @Override
    public Long getCurrentProfileId() {
        return getCurrentAuth().getProfileId();
    }

    @Override
    public LoginResponse.UserSummary getCurrentUserSummary() {
        JwtAuthenticationToken auth = getCurrentAuth();
        return LoginResponse.UserSummary.builder()
                .id(auth.getUserId())
                .username(auth.getUsername())
                .role(auth.getRole())
                .profileId(auth.getProfileId())
                .build();
    }

    @Override
    public boolean isAdmin() {
        return "ADMIN".equals(getCurrentRole());
    }

    @Override
    public boolean isTeacher() {
        return "TEACHER".equals(getCurrentRole());
    }

    @Override
    public boolean isStudent() {
        return "STUDENT".equals(getCurrentRole());
    }

    @Override
    public boolean isTeacherOwner(Long teacherId) {
        return teacherId.equals(getCurrentProfileId());
    }

    @Override
    public boolean isStudentOwner(Long studentId) {
        return studentId.equals(getCurrentProfileId());
    }

    @Override
    public boolean isTeacherAssignedToClass(Long classId) {
        Long profileId = getCurrentProfileId();
        if (profileId == null) return false;
        return teacherRepository.existsByIdAndClasses_Id(profileId, classId);
    }

    @Override
    public boolean isTeacherAssignedToSubject(Long subjectId) {
        Long profileId = getCurrentProfileId();
        if (profileId == null) return false;
        return teacherRepository.existsByIdAndSubjects_Id(profileId, subjectId);
    }
}