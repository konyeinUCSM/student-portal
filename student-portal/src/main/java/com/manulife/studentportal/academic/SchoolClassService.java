package com.manulife.studentportal.academic;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.academic.web.CreateClassRequest;
import com.manulife.studentportal.academic.web.UpdateClassRequest;
import com.manulife.studentportal.academic.web.SchoolClassResponse;
import com.manulife.studentportal.student.StudentInfo;

public interface SchoolClassService {

    SchoolClassResponse create(CreateClassRequest request);

    SchoolClassResponse getById(Long id);

    SchoolClassInfo getClassInfoById(Long id);

    Page<SchoolClassResponse> getAll(Pageable pageable);

    SchoolClassResponse update(Long id, UpdateClassRequest request);

    void delete(Long id);

    List<StudentInfo> getStudents(Long id, Pageable pageable);
}
