package com.manulife.studentportal.entity;

import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "marks", indexes = {
                @Index(name = "idx_mark_student_id", columnList = "student_id"),
                @Index(name = "idx_mark_exam_id", columnList = "exam_id")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_mark_student_exam", columnNames = { "student_id", "exam_id" })
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