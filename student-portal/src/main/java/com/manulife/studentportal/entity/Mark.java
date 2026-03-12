package com.manulife.studentportal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "marks", indexes = {
        @Index(name = "idx_mark_student_id", columnList = "student_id"),
        @Index(name = "idx_mark_exam_id", columnList = "exam_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_mark_student_exam",
                columnNames = {"student_id", "exam_id"})
})
@SQLRestriction("deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mark extends BaseEntity {

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false, length = 2)
    private String grade;

    @Column(length = 255)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
}