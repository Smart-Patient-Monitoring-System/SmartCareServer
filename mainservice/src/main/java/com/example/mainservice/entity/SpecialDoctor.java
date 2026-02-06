package com.example.mainservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialDoctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Auto-generated primary key

    @Column(nullable = false, unique = true, length = 50)
    private String registrationNumber; // Hospital/medical registration number

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialty;

    @Column(nullable = false)
    private Double consultationFee;

    private String profilePhoto;
    private String description;

    @Column(nullable = false)
    private String qualification;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;


    //one doctor have many available date and time
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<DoctorAvailability> availabilities;
}