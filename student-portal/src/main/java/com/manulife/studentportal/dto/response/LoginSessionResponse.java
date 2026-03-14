package com.manulife.studentportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginSessionResponse {

    private Long id;

    private Long userId;

    private String username;

    private String role;

    private LocalDateTime loginTime;

    private LocalDateTime expiryTime;

    private String ipAddress;

    private Boolean active;
}
