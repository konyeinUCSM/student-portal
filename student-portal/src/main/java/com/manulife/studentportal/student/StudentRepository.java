package com.manulife.studentportal.student;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);

    List<Student> findByClassId(Long classId, Pageable pageable);

    Page<Student> findByClassIdIn(java.util.Collection<Long> classIds, Pageable pageable);

    // Include deleted records — rollNumber is permanently reserved once used
    @Query(value = "SELECT COUNT(*) FROM students WHERE roll_number = ?1", nativeQuery = true)
    long countByRollNumberAllRecords(String rollNumber);

    boolean existsByUserId(Long userId);

    boolean existsByClassId(Long classId);
}