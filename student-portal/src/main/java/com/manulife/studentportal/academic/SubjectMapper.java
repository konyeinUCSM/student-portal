package com.manulife.studentportal.academic;

import java.util.List;

import org.mapstruct.Mapper;

import com.manulife.studentportal.academic.web.SubjectResponse;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    SubjectResponse toResponse(Subject subject);

    List<SubjectResponse> toResponseList(List<Subject> subjects);
}
