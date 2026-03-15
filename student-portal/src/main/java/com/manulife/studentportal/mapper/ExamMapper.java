package com.manulife.studentportal.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.manulife.studentportal.dto.response.ExamResponse;
import com.manulife.studentportal.entity.Exam;

@Mapper(componentModel = "spring")
public interface ExamMapper {

    @Mapping(source = "schoolClass.id", target = "classId")
    @Mapping(source = "schoolClass.name", target = "className")
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "subject.name", target = "subjectName")
    ExamResponse toResponse(Exam exam);

    List<ExamResponse> toResponseList(List<Exam> exams);
}
