package com.example.mainservice.entity;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor

public class PendingDoctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // DB primary key

    @Column(nullable = false)
    private String name;

    private LocalDate dateOfBirth;

    private String address;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nicNo;

    private String gender;

    @Column(nullable = false)
    private String contactNo;

    @Column(nullable = false, unique = true)
    private String doctorRegNo;   // Doctor’s ID (form field)

    private String position;   // Doctor's Position

    private String hospital;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    public PendingDoctor() {

    }
}
