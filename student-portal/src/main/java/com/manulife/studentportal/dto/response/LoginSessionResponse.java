package com.manulife.studentportal.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
