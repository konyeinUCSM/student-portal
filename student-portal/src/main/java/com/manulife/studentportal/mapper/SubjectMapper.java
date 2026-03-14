package com.manulife.studentportal.mapper;

import com.manulife.studentportal.dto.response.SubjectResponse;
import com.manulife.studentportal.entity.Subject;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    SubjectResponse toResponse(Subject subject);

    List<SubjectResponse> toResponseList(List<Subject> subjects);
}
