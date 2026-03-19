package com.manulife.studentportal.entity;

import java.util.HashSet;
import java.util.Set;

import com.manulife.studentportal.shared.entity.SoftDeletableEntity;
import com.manulife.studentportal.user.internal.User;

import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "teachers", indexes = {
                @Index(name = "idx_teacher_staff_id", columnList = "staffId", unique = true)
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

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false, unique = true)
        private User user;

        @ManyToMany
        @JoinTable(name = "teacher_class", joinColumns = @JoinColumn(name = "teacher_id"), inverseJoinColumns = @JoinColumn(name = "class_id"))
        @Builder.Default
        private Set<SchoolClass> classes = new HashSet<>();

        @ManyToMany
        @JoinTable(name = "teacher_subject", joinColumns = @JoinColumn(name = "teacher_id"), inverseJoinColumns = @JoinColumn(name = "subject_id"))
        @Builder.Default
        private Set<Subject> subjects = new HashSet<>();
}