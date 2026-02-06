package com.example.mainservice.service;

import com.example.mainservice.dto.DoctorDTO;
import com.example.mainservice.dto.PendingDoctorDTO;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.entity.PendingDoctor;
import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PendingDoctorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PendingDoctorService {
    private final PendingDoctorRepo pendingdoctorrepo;
    private final DoctorRepo doctorRepo;
    private final PasswordEncoder passwordEncoder;

    public PendingDoctor create(PendingDoctorDTO pendingdoctor){

        PendingDoctor pd=PendingDoctor.builder()
                .name(pendingdoctor.getName())
                .email(pendingdoctor.getEmail())
                .nicNo(pendingdoctor.getNicNo())
                .doctorRegNo(pendingdoctor.getDoctorRegNo())
                .address(pendingdoctor.getAddress())
                .gender(pendingdoctor.getGender())
                .contactNo(pendingdoctor.getContactNo())
                .hospital(pendingdoctor.getHospital())
                .password(passwordEncoder.encode(pendingdoctor.getPassword()))
                .position(pendingdoctor.getPosition())
                .username(pendingdoctor.getUsername())
                .dateOfBirth(pendingdoctor.getDateOfBirth())
                .build();
        return  pendingdoctorrepo.save(pd);
    }
    public List<PendingDoctorDTO> getDetails(){

        return pendingdoctorrepo.findAll().stream().map(pd -> PendingDoctorDTO.builder()
                .Id(pd.getId())
                .name(pd.getName())
                .email(pd.getEmail())
                .nicNo(pd.getNicNo())
                .doctorRegNo(pd.getDoctorRegNo())
                .address(pd.getAddress())
                .gender(pd.getGender())
                .contactNo(pd.getContactNo())
                .hospital(pd.getHospital())
                .password(pd.getPassword())
                .position(pd.getPosition())
                .username(pd.getUsername())
                .dateOfBirth(pd.getDateOfBirth()).build()).toList();
    }

    public void deletePendingDoctor(Long Id){

        pendingdoctorrepo.deleteById(Id);
    }

    public PendingDoctorDTO updatePendingDoctor(Long Id,PendingDoctorDTO dto){
        PendingDoctor pd = pendingdoctorrepo.findById(Id).orElseThrow();

        if(dto.getDoctorRegNo()!=null) pd.setDoctorRegNo(dto.getDoctorRegNo());
        if(dto.getEmail()!=null) pd.setEmail(dto.getEmail());
        if(dto.getDateOfBirth()!=null) pd.setDateOfBirth(dto.getDateOfBirth());
        if(dto.getContactNo()!=null) pd.setContactNo(dto.getContactNo());
        if(dto.getAddress()!=null) pd.setAddress(dto.getAddress());
        if(dto.getGender()!=null) pd.setGender(dto.getGender());
        if(dto.getHospital()!=null) pd.setHospital(dto.getHospital());
        if(dto.getName()!=null) pd.setName(dto.getName());
        if(dto.getNicNo()!=null) pd.setNicNo(dto.getNicNo());
        if(dto.getPassword()!=null) pd.setPassword(dto.getPassword());
        if(dto.getPosition()!=null) pd.setPosition(dto.getPosition());
        if(dto.getUsername()!=null) pd.setUsername(dto.getUsername());

        PendingDoctor updatedPendingDoctor = pendingdoctorrepo.save(pd);
        return convertToDTO(updatedPendingDoctor);
    }

    private PendingDoctorDTO convertToDTO(PendingDoctor pendingdoctor) {
        PendingDoctorDTO dto = new PendingDoctorDTO();
        dto.setId(pendingdoctor.getId());
        dto.setDoctorRegNo(pendingdoctor.getDoctorRegNo());
        dto.setName(pendingdoctor.getName());
        dto.setEmail(pendingdoctor.getEmail());
        dto.setDateOfBirth(pendingdoctor.getDateOfBirth());
        dto.setContactNo(pendingdoctor.getContactNo());
        dto.setAddress(pendingdoctor.getAddress());
        dto.setGender(pendingdoctor.getGender());
        dto.setHospital(pendingdoctor.getHospital());
        dto.setNicNo(pendingdoctor.getNicNo());
        dto.setPassword(pendingdoctor.getPassword());
        dto.setPosition(pendingdoctor.getPosition());
        dto.setUsername(pendingdoctor.getUsername());

        return dto;
    }

    @Transactional
    public void acceptDoctor(Long id) {

        PendingDoctor pending = pendingdoctorrepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Pending doctor not found"));

        Doctor doctor = Doctor.builder()
                .name(pending.getName())
                .dateOfBirth(pending.getDateOfBirth())
                .address(pending.getAddress())
                .email(pending.getEmail())
                .nicNo(pending.getNicNo())
                .gender(pending.getGender())
                .contactNo(pending.getContactNo())
                .doctorRegNo(pending.getDoctorRegNo())
                .position(pending.getPosition())
                .hospital(pending.getHospital())
                .username(pending.getUsername())
                .password(pending.getPassword()) // already encrypted
                .build();

        doctorRepo.save(doctor);     //  add to Doctor table
        pendingdoctorrepo.deleteById(id);  //  remove from Pending table
    }

}
