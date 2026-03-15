package com.manulife.studentportal.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.manulife.studentportal.entity.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    Page<Exam> findBySchoolClassId(Long classId, Pageable pageable);

    Page<Exam> findBySchoolClassIdAndSubjectId(Long classId, Long subjectId, Pageable pageable);

    Page<Exam> findBySchoolClassIdIn(List<Long> classIds, Pageable pageable);

    boolean existsByNameAndSchoolClassIdAndSubjectId(String name, Long classId, Long subjectId);
}
