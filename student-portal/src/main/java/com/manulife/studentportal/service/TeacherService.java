package com.manulife.studentportal.service;

import com.manulife.studentportal.dto.request.AssignClassesRequest;
import com.manulife.studentportal.dto.request.AssignSubjectsRequest;
import com.manulife.studentportal.dto.request.CreateTeacherRequest;
import com.manulife.studentportal.dto.request.UpdateTeacherRequest;
import com.manulife.studentportal.dto.response.SchoolClassResponse;
import com.manulife.studentportal.dto.response.SubjectResponse;
import com.manulife.studentportal.dto.response.TeacherResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeacherService {

    TeacherResponse create(CreateTeacherRequest request);

    TeacherResponse getById(Long id);

    Page<TeacherResponse> getAll(Pageable pageable);

    TeacherResponse update(Long id, UpdateTeacherRequest request);

    void delete(Long id);

    void assignClasses(Long id, AssignClassesRequest request);

    void assignSubjects(Long id, AssignSubjectsRequest request);

    List<SchoolClassResponse> getClasses(Long id);

    List<SubjectResponse> getSubjects(Long id);
}
