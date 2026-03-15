package com.manulife.studentportal.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.manulife.studentportal.dto.response.SubjectResponse;
import com.manulife.studentportal.entity.Subject;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    SubjectResponse toResponse(Subject subject);

    List<SubjectResponse> toResponseList(List<Subject> subjects);
}
