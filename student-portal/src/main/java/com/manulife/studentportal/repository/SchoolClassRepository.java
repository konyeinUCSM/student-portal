package com.manulife.studentportal.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.manulife.studentportal.entity.SchoolClass;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    boolean existsByName(String name);

    Optional<SchoolClass> findByName(String name);

    @Query(value = "SELECT c FROM Teacher t JOIN t.classes c WHERE t.id = :teacherId",
           countQuery = "SELECT COUNT(c) FROM Teacher t JOIN t.classes c WHERE t.id = :teacherId")
    Page<SchoolClass> findByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);
}
