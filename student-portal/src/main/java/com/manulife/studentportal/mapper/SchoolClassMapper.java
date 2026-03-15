package com.manulife.studentportal.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.manulife.studentportal.dto.response.SchoolClassResponse;
import com.manulife.studentportal.entity.SchoolClass;

@Mapper(componentModel = "spring")
public interface SchoolClassMapper {

    @Mapping(target = "studentCount", constant = "0")
    SchoolClassResponse toResponse(SchoolClass schoolClass);

    List<SchoolClassResponse> toResponseList(List<SchoolClass> schoolClasses);
}
