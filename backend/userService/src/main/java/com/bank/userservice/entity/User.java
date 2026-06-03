package com.bank.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @NotBlank
    @Column(name = "first_name", length = 100)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", length = 100)
    private String lastName;

    @NotBlank
    @Column(name = "email", unique = true, length = 150)
    private String email;

    @NotBlank
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @NotNull
    @Column(name = "status", length = 30)
    private String status = "ACTIVE";

    //dont use @NotNull for the next 2 or they wont work
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    //@JoinColumn()
    @ManyToMany
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role){ this.roles.add(role); }
}
