package com.example.mainservice.service;
import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.repository.DoctorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepo doctorrepo;
    private final PasswordEncoder passwordEncoder;

    public Doctor create(DoctorDTO doctor){

        Doctor d=Doctor.builder()
                .name(doctor.getName())
                .email(doctor.getEmail())
                .nicNo(doctor.getNicNo())
                .doctorRegNo(doctor.getDoctorRegNo())
                .address(doctor.getAddress())
                .gender(doctor.getGender())
                .contactNo(doctor.getContactNo())
                .hospital(doctor.getHospital())
                .password(passwordEncoder.encode(doctor.getPassword()))
                .position(doctor.getPosition())
                .username(doctor.getUsername())
                .dateOfBirth(doctor.getDateOfBirth())
                .build();
        return  doctorrepo.save(d);

    }

    public List<DoctorDTO> getDetails(){

        return doctorrepo.findAll().stream().map(d -> DoctorDTO.builder()
                .Id(d.getId())
                .name(d.getName())
                .email(d.getEmail())
                .nicNo(d.getNicNo())
                .doctorRegNo(d.getDoctorRegNo())
                .address(d.getAddress())
                .gender(d.getGender())
                .contactNo(d.getContactNo())
                .hospital(d.getHospital())
                .password(d.getPassword())
                .position(d.getPosition())
                .username(d.getUsername())
                .dateOfBirth(d.getDateOfBirth()).build()).toList();
    }
    public void deleteDoctor(Long Id){

        doctorrepo.deleteById(Id);
    }

    public DoctorDTO updateDoctor(Long Id,DoctorDTO dto){
        Doctor d = doctorrepo.findById(Id).orElseThrow();

        if(dto.getDoctorRegNo()!=null) d.setDoctorRegNo(dto.getDoctorRegNo());
        if(dto.getEmail()!=null) d.setEmail(dto.getEmail());
        if(dto.getDateOfBirth()!=null) d.setDateOfBirth(dto.getDateOfBirth());
        if(dto.getContactNo()!=null) d.setContactNo(dto.getContactNo());
        if(dto.getAddress()!=null) d.setAddress(dto.getAddress());
        if(dto.getGender()!=null) d.setGender(dto.getGender());
        if(dto.getHospital()!=null) d.setHospital(dto.getHospital());
        if(dto.getName()!=null) d.setName(dto.getName());
        if(dto.getNicNo()!=null) d.setNicNo(dto.getNicNo());
        if(dto.getPassword()!=null) d.setPassword(dto.getPassword());
        if(dto.getPosition()!=null) d.setPosition(dto.getPosition());
        if(dto.getUsername()!=null) d.setUsername(dto.getUsername());

        Doctor updatedDoctor = doctorrepo.save(d);
        return convertToDTO(updatedDoctor);


    }

    private DoctorDTO convertToDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setDoctorRegNo(doctor.getDoctorRegNo());
        dto.setName(doctor.getName());
        dto.setEmail(doctor.getEmail());
        dto.setDateOfBirth(doctor.getDateOfBirth());
        dto.setContactNo(doctor.getContactNo());
        dto.setAddress(doctor.getAddress());
        dto.setGender(doctor.getGender());
        dto.setHospital(doctor.getHospital());
        dto.setNicNo(doctor.getNicNo());
        dto.setPassword(doctor.getPassword());
        dto.setPosition(doctor.getPosition());
        dto.setUsername(doctor.getUsername());

        return dto;
    }
}
