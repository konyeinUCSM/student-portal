package com.manulife.studentportal.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.manulife.studentportal.dto.response.TeacherResponse;
import com.manulife.studentportal.entity.SchoolClass;
import com.manulife.studentportal.entity.Subject;
import com.manulife.studentportal.entity.Teacher;

@Mapper(componentModel = "spring")
public interface TeacherMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "classes", target = "classNames", qualifiedByName = "classesToNames")
    @Mapping(source = "subjects", target = "subjectNames", qualifiedByName = "subjectsToNames")
    TeacherResponse toResponse(Teacher teacher);

    List<TeacherResponse> toResponseList(List<Teacher> teachers);

    @Named("classesToNames")
    default List<String> classesToNames(Set<SchoolClass> classes) {
        if (classes == null || classes.isEmpty()) {
            return List.of();
        }
        return classes.stream()
                .map(SchoolClass::getName)
                .collect(Collectors.toList());
    }

    @Named("subjectsToNames")
    default List<String> subjectsToNames(Set<Subject> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return List.of();
        }
        return subjects.stream()
                .map(Subject::getName)
                .collect(Collectors.toList());
    }
}
