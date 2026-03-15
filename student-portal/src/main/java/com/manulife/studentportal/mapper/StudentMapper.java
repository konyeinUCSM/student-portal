package com.manulife.studentportal.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.manulife.studentportal.dto.response.StudentResponse;
import com.manulife.studentportal.entity.Student;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "schoolClass.id", target = "classId")
    @Mapping(source = "schoolClass.name", target = "className")
    StudentResponse toResponse(Student student);

    List<StudentResponse> toResponseList(List<Student> students);
}
