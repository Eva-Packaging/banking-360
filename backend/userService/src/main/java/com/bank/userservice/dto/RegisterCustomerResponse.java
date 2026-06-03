package com.bank.userservice.dto;

import lombok.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterCustomerResponse {
        private String userId;
        private String firstName;
        private String lastName;
        private String email;
        private Set<String> roles;
        private String status;
        private LocalDateTime createdAt;
}
