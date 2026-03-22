package com.manulife.studentportal.teacher.internal;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.manulife.studentportal.academic.SchoolClassService;
import com.manulife.studentportal.academic.SubjectService;
import com.manulife.studentportal.teacher.Teacher;
import com.manulife.studentportal.teacher.web.TeacherResponse;
import com.manulife.studentportal.user.UserQueryService;

@Mapper(componentModel = "spring")
public abstract class TeacherMapper {

    @Autowired
    protected SchoolClassService schoolClassService;

    @Autowired
    protected SubjectService subjectService;

    @Autowired
    protected UserQueryService userQueryService;

    @Mapping(source = "userId", target = "userId")
    @Mapping(target = "username", expression = "java(getUserUsername(teacher.getUserId()))")
    @Mapping(target = "classNames", expression = "java(getClassNames(teacher.getClassIds()))")
    @Mapping(target = "subjectNames", expression = "java(getSubjectNames(teacher.getSubjectIds()))")
    public abstract TeacherResponse toResponse(Teacher teacher);

    public abstract List<TeacherResponse> toResponseList(List<Teacher> teachers);

    protected List<String> getClassNames(Set<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return List.of();
        }
        return classIds.stream()
                .map(id -> {
                    try {
                        return schoolClassService.getClassInfoById(id).name();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(name -> name != null)
                .collect(Collectors.toList());
    }

    protected List<String> getSubjectNames(Set<Long> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return List.of();
        }
        return subjectIds.stream()
                .map(id -> {
                    try {
                        return subjectService.getSubjectInfoById(id).name();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(name -> name != null)
                .collect(Collectors.toList());
    }

    protected String getUserUsername(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            return userQueryService.findById(userId)
                    .map(user -> user.username())
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
