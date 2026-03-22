package com.manulife.studentportal.teacher;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.teacher.web.AssignClassesRequest;
import com.manulife.studentportal.teacher.web.AssignSubjectsRequest;
import com.manulife.studentportal.teacher.web.CreateTeacherRequest;
import com.manulife.studentportal.teacher.web.UpdateTeacherRequest;
import com.manulife.studentportal.academic.SchoolClassInfo;
import com.manulife.studentportal.academic.SubjectInfo;
import com.manulife.studentportal.teacher.web.TeacherResponse;

public interface TeacherService {

    TeacherResponse create(CreateTeacherRequest request);

    TeacherResponse getById(Long id);

    Page<TeacherResponse> getAll(Pageable pageable);

    TeacherResponse update(Long id, UpdateTeacherRequest request);

    void delete(Long id);

    void assignClasses(Long id, AssignClassesRequest request);

    void assignSubjects(Long id, AssignSubjectsRequest request);

    List<SchoolClassInfo> getClasses(Long id);

    List<SubjectInfo> getSubjects(Long id);
}
