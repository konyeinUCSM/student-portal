package com.manulife.studentportal.academic;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    Page<Exam> findBySchoolClassId(Long classId, Pageable pageable);

    Page<Exam> findBySchoolClassIdAndSubjectId(Long classId, Long subjectId, Pageable pageable);

    Page<Exam> findBySchoolClassIdIn(List<Long> classIds, Pageable pageable);

    boolean existsByNameAndSchoolClassIdAndSubjectId(String name, Long classId, Long subjectId);

    boolean existsBySchoolClassId(Long classId);

    boolean existsBySubjectId(Long subjectId);

    @Query("SELECT e.id FROM Exam e WHERE e.schoolClass.id IN :classIds")
    List<Long> findIdsBySchoolClassIdIn(@Param("classIds") List<Long> classIds);
}
