package com.manulife.studentportal.academic;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.manulife.studentportal.academic.web.SchoolClassResponse;

@Mapper(componentModel = "spring")
public interface SchoolClassMapper {

    @Mapping(target = "studentCount", constant = "0")
    SchoolClassResponse toResponse(SchoolClass schoolClass);

    List<SchoolClassResponse> toResponseList(List<SchoolClass> schoolClasses);
}
