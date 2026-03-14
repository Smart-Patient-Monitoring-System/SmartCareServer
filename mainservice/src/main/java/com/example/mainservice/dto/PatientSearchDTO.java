package com.example.mainservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatientSearchDTO {
    private Long id;
    private String name;
    private String nicNo;
    private String email;
    private String contactNo;
    private String gender;
    private String avatar;
}
