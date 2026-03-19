package com.manulife.studentportal.entity;

import java.time.LocalDate;

import org.hibernate.annotations.SQLRestriction;

import com.manulife.studentportal.shared.entity.SoftDeletableEntity;

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
@Table(name = "exams", indexes = {
                @Index(name = "idx_exam_class_subject", columnList = "class_id, subject_id")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_exam_name_class_subject", columnNames = { "name", "class_id",
                                "subject_id" })
})
@SQLRestriction("deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Exam extends SoftDeletableEntity {

        @Column(nullable = false, length = 100)
        private String name;

        private LocalDate examDate;

        @Column(nullable = false)
        @Builder.Default
        private Integer fullMarks = 100;

        @Column(nullable = false)
        @Builder.Default
        private Integer passMarks = 40;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "class_id", nullable = false)
        private SchoolClass schoolClass;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "subject_id", nullable = false)
        private Subject subject;
}