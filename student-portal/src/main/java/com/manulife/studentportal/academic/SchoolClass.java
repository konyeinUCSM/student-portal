package com.manulife.studentportal.academic;

import org.hibernate.annotations.SQLRestriction;

import com.manulife.studentportal.shared.entity.SoftDeletableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "classes")
@SQLRestriction("deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClass extends SoftDeletableEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}