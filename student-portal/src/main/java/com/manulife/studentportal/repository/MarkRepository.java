package com.manulife.studentportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.manulife.studentportal.entity.Mark;

@Repository
public interface MarkRepository extends JpaRepository<Mark, Long> {

    Page<Mark> findByStudentId(Long studentId, Pageable pageable);

    Page<Mark> findByExamId(Long examId, Pageable pageable);

    Optional<Mark> findByStudentIdAndExamId(Long studentId, Long examId);

    boolean existsByStudentIdAndExamId(Long studentId, Long examId);

    Page<Mark> findByExamIdIn(List<Long> examIds, Pageable pageable);

    List<Mark> findByStudentId(Long studentId);
}
