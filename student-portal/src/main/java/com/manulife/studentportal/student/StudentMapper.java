package com.manulife.studentportal.student;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.manulife.studentportal.student.web.StudentResponse;
import com.manulife.studentportal.user.UserQueryService;

@Mapper(componentModel = "spring")
public abstract class StudentMapper {

    @Autowired
    protected UserQueryService userQueryService;

    @Mapping(source = "userId", target = "userId")
    @Mapping(target = "username", expression = "java(getUserUsername(student.getUserId()))")
    @Mapping(source = "classId", target = "classId")
    @Mapping(target = "className", constant = "N/A")
    public abstract StudentResponse toResponse(Student student);

    public abstract List<StudentResponse> toResponseList(List<Student> students);

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
