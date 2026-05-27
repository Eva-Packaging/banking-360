package com.bank.userService.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "first_name", length = 100)
    private String firstName;

    @NotNull
    @Column(name = "last_name", length = 100)
    private String lastName;

    @NotNull
    @Column(name = "email", unique = true, length = 150)
    private String email;

    @NotNull
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @NotNull
    @Column(name = "status")
    private Boolean active = true;

    @NotNull
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
