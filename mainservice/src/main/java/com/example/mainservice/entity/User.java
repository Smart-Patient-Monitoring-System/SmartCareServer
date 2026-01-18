package com.example.mainservice.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // PATIENT, DOCTOR, ADMIN

    @Column(name = "profile_picture")
    private String profilePicture; // or 'avatar' - adjust field name

    @Column(name = "phone")
    private String phone;

    // Add other fields as needed...

    public enum Role {
        PATIENT,
        DOCTOR,
        ADMIN
    }
}