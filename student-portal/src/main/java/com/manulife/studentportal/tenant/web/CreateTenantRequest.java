package com.manulife.studentportal.tenant.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequest {

    @NotBlank(message = "Tenant name is required")
    @Size(max = 100, message = "Tenant name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Schema name is required")
    @Size(max = 63, message = "Schema name must not exceed 63 characters")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "Schema name must start with a letter and contain only lowercase letters, digits, and underscores")
    private String schemaName;
}
