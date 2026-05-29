package com.bank.userservice.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterCustomerResponse {
        private String userId;
        private String firstName;
        private String lastName;
        private String email;
        private String role;
        private String status;
        private Instant createdAt;
}
