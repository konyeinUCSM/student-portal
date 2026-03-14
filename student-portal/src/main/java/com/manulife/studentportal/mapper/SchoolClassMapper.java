package com.manulife.studentportal.mapper;

import com.manulife.studentportal.dto.response.SchoolClassResponse;
import com.manulife.studentportal.entity.SchoolClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SchoolClassMapper {

    @Mapping(target = "studentCount", constant = "0")
    SchoolClassResponse toResponse(SchoolClass schoolClass);

    List<SchoolClassResponse> toResponseList(List<SchoolClass> schoolClasses);
}
