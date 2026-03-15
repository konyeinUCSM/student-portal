package com.manulife.studentportal.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseEntity {

    @Column(nullable = false)
    private boolean deleted = false;

    @Column
    private LocalDateTime deletedAt;

    @Column(length = 50)
    private String deletedBy;

    public void softDelete(String username) {
        if (this.deleted) {
            throw new IllegalStateException(
                    "Entity is already soft-deleted: " + getClass().getSimpleName() + " id=" + getId());
        }
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = username;
    }
}
