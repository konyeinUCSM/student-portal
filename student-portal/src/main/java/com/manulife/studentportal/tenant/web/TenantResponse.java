package com.manulife.studentportal.tenant.web;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    private Long id;
    private String name;
    private String schemaName;
    private boolean active;
    private LocalDateTime createdAt;
}
