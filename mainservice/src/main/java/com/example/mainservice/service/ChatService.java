package com.example.mainservice.service;

import com.example.mainservice.dto.ChatMessageDTO;
import com.example.mainservice.dto.DoctorSearchDTO;
import com.example.mainservice.entity.ChatMessage;
import com.example.mainservice.entity.Conversation;
import com.example.mainservice.entity.Doctor;
import com.example.mainservice.repository.ChatMessageRepository;
import com.example.mainservice.repository.ConversationRepository;
import com.example.mainservice.repository.DoctorRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final DoctorRepo doctorRepo;

    @Transactional
    public ChatMessage saveMessage(ChatMessageDTO messageDTO) {
        Conversation conversation = conversationRepository
                .findById(messageDTO.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        ChatMessage chatMessage = ChatMessage.builder()
                .conversation(conversation)
                .senderId(messageDTO.getSenderId())
                .receiverId(messageDTO.getReceiverId())
                .content(messageDTO.getContent())
                .type(messageDTO.getType())
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        // map attachments if provided
        if (messageDTO.getAttachments() != null && !messageDTO.getAttachments().isEmpty()) {
            var attList = messageDTO.getAttachments().stream().map(a ->
                    com.example.mainservice.entity.ChatAttachment.builder()
                            .fileName(a.getFileName())
                            .url(a.getUrl())
                            .contentType(a.getContentType())
                            .size(a.getSize())
                            .message(chatMessage)
                            .build()
            ).toList();

            chatMessage.setAttachments(attList);
        }

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        conversation.setLastMessage(messageDTO.getContent());
        conversation.setTimestamp(LocalDateTime.now());
        conversationRepository.save(conversation);

        log.info("Message saved with ID: {}", savedMessage.getId());
        return savedMessage;
    }

    public List<ChatMessage> getConversationMessages(Long conversationId) {
        return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    @Transactional
    public void markMessagesAsRead(Long conversationId, Long userId) {
        List<ChatMessage> unreadMessages = chatMessageRepository
                .findByConversationIdAndReceiverIdAndReadFalse(conversationId, userId);

        unreadMessages.forEach(msg -> msg.setRead(true));
        chatMessageRepository.saveAll(unreadMessages);

        log.info("Marked {} messages as read for conversation {}", unreadMessages.size(), conversationId);
    }

    public Integer getUnreadCount(Long conversationId, Long userId) {
        Integer count = chatMessageRepository.countByConversationIdAndReceiverIdAndReadFalse(conversationId, userId);
        return count != null ? count : 0;
    }

    public List<Conversation> getUserConversations(Long userId) {
        return conversationRepository.findAllByUserId(userId);
    }

    @Transactional
    public Conversation createConversation(Long patientId, Long doctorId) {
        // NO int conversion. Keep IDs Long everywhere.
        return conversationRepository.findByPatientIdAndDoctorId(patientId, doctorId)
                .orElseGet(() -> {
                    log.info("Creating new conversation between patient {} and doctor {}", patientId, doctorId);
                    Conversation newConversation = Conversation.builder()
                            .patientId(patientId)
                            .doctorId(doctorId)
                            .timestamp(LocalDateTime.now())
                            .build();
                    return conversationRepository.save(newConversation);
                });
    }

    public Integer getTotalUnreadCount(Long userId) {
        Integer count = chatMessageRepository.countByReceiverIdAndReadFalse(userId);
        return count != null ? count : 0;
    }

    // ========= Doctor search =========

    public List<DoctorSearchDTO> searchDoctors(String searchTerm) {
        log.info("Searching for doctors with term: {}", searchTerm);

        List<Doctor> doctors;
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            doctors = doctorRepo.findAll();
        } else {
            doctors = doctorRepo.findByNameContainingIgnoreCaseOrDoctorRegNoContaining(
                    searchTerm.trim(),
                    searchTerm.trim()
            );
        }

        return doctors.stream()
                .map(this::convertToDoctorSearchDTO)
                .collect(Collectors.toList());
    }

    public List<DoctorSearchDTO> getAllDoctors() {
        log.info("Fetching all doctors for chat");
        return doctorRepo.findAll().stream()
                .map(this::convertToDoctorSearchDTO)
                .collect(Collectors.toList());
    }

    public DoctorSearchDTO getDoctorById(Long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));
        return convertToDoctorSearchDTO(doctor);
    }

    public List<DoctorSearchDTO> searchDoctorsByHospital(String hospital) {
        log.info("Searching doctors by hospital: {}", hospital);

        List<Doctor> doctors = doctorRepo.findByHospitalContainingIgnoreCase(hospital);

        return doctors.stream()
                .map(this::convertToDoctorSearchDTO)
                .collect(Collectors.toList());
    }

    private DoctorSearchDTO convertToDoctorSearchDTO(Doctor doctor) {
        return DoctorSearchDTO.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .doctorRegNo(doctor.getDoctorRegNo())
                .hospital(doctor.getHospital())
                .position(doctor.getPosition())
                .email(doctor.getEmail())
                .contactNo(doctor.getContactNo())
                .build();
    }
}
