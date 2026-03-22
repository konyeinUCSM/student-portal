package com.manulife.studentportal.teacher;

import java.util.HashSet;
import java.util.Set;

import com.manulife.studentportal.shared.entity.SoftDeletableEntity;

import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "teachers", indexes = {
                @Index(name = "idx_teacher_staff_id", columnList = "staffId", unique = true),
                @Index(name = "idx_teacher_user_id", columnList = "user_id", unique = true)
})
@SQLRestriction("deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Teacher extends SoftDeletableEntity {

        @Column(nullable = false, length = 100)
        private String name;

        @Column(nullable = false, unique = true, length = 20)
        private String staffId;

        @Column(length = 20)
        private String phone;

        @Column(name = "user_id", nullable = false, unique = true)
        private Long userId;

        @ElementCollection
        @CollectionTable(name = "teacher_class", joinColumns = @JoinColumn(name = "teacher_id"))
        @Column(name = "class_id")
        @Builder.Default
        private Set<Long> classIds = new HashSet<>();

        @ElementCollection
        @CollectionTable(name = "teacher_subject", joinColumns = @JoinColumn(name = "teacher_id"))
        @Column(name = "subject_id")
        @Builder.Default
        private Set<Long> subjectIds = new HashSet<>();
}