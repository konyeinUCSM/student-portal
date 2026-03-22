package com.manulife.studentportal.student;

import java.time.LocalDate;

import org.hibernate.annotations.SQLRestriction;

import com.manulife.studentportal.shared.entity.SoftDeletableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_student_roll_number", columnList = "rollNumber", unique = true),
        @Index(name = "idx_student_class_id", columnList = "class_id"),
        @Index(name = "idx_student_user_id", columnList = "user_id", unique = true)
})
@SQLRestriction("deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student extends SoftDeletableEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String rollNumber;

    @Column(length = 20)
    private String phone;

    private LocalDate dateOfBirth;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "class_id", nullable = false)
    private Long classId;
}