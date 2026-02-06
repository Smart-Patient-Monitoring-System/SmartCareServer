package com.example.mainservice.controller;

import com.example.mainservice.dto.ChatAttachmentDTO;
import com.example.mainservice.dto.ChatMessageDTO;
import com.example.mainservice.dto.ConversationDTO;
import com.example.mainservice.dto.DoctorSearchDTO;
import com.example.mainservice.entity.*;
import com.example.mainservice.repository.DoctorRepo;
import com.example.mainservice.repository.PatientRepo;
import com.example.mainservice.repository.UserRepository;
import com.example.mainservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatRestController {

    private final ChatService chatService;
    private final PatientRepo patientRepo;
    private final DoctorRepo doctorRepo;


    // ========== EXISTING ENDPOINTS ==========

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        List<Conversation> conversations = chatService.getUserConversations(userId);

        List<ConversationDTO> conversationDTOs = conversations.stream()
                .map(conv -> mapToConversationDTO(conv, userId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(conversationDTOs);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(
            @PathVariable Long conversationId,
            Authentication authentication) {

        // Keep Long (not Integer)
        Long userId = getCurrentUserId(authentication);

        List<ChatMessage> messages = chatService.getConversationMessages(conversationId);
        List<ChatMessageDTO> messageDTOs = messages.stream()
                .map(this::mapToChatMessageDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(messageDTOs);
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long conversationId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        chatService.markMessagesAsRead(conversationId, userId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestBody ConversationDTO conversationDTO,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);

        Long patientId = conversationDTO.getPatient().getId();
        Long doctorId = conversationDTO.getDoctor().getId();

        Conversation conversation = chatService.createConversation(patientId, doctorId);

        return ResponseEntity.ok(mapToConversationDTO(conversation, userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Integer count = chatService.getTotalUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    // ========== DOCTOR SEARCH ENDPOINTS ==========

    @GetMapping("/doctors/search")
    public ResponseEntity<List<DoctorSearchDTO>> searchDoctors(
            @RequestParam(required = false) String query) {

        log.info("Search request received with query: {}", query);
        List<DoctorSearchDTO> doctors = chatService.searchDoctors(query);

        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorSearchDTO>> getAllDoctors() {
        log.info("Fetching all doctors");
        List<DoctorSearchDTO> doctors = chatService.getAllDoctors();

        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<DoctorSearchDTO> getDoctorById(@PathVariable Long doctorId) {
        log.info("Fetching doctor with ID: {}", doctorId);
        DoctorSearchDTO doctor = chatService.getDoctorById(doctorId);

        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/doctors/hospital")
    public ResponseEntity<List<DoctorSearchDTO>> searchDoctorsByHospital(
            @RequestParam String name) {

        log.info("Searching doctors by hospital: {}", name);
        List<DoctorSearchDTO> doctors = chatService.searchDoctorsByHospital(name);

        return ResponseEntity.ok(doctors);
    }

    @PostMapping("/attachments/upload")
    public ResponseEntity<List<ChatAttachmentDTO>> upload(@RequestParam("files") List<MultipartFile> files) throws IOException {
        List<ChatAttachmentDTO> result = new ArrayList<>();

        for (MultipartFile f : files) {
            String savedName = UUID.randomUUID() + "_" + f.getOriginalFilename();
            Path target = Paths.get("uploads").resolve(savedName);
            Files.createDirectories(target.getParent());
            Files.copy(f.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = "http://localhost:8080/uploads/" + savedName;

            result.add(ChatAttachmentDTO.builder()
                    .fileName(f.getOriginalFilename())
                    .url(url)
                    .contentType(f.getContentType())
                    .size(f.getSize())
                    .build());
        }

        return ResponseEntity.ok(result);
    }


    /**
     * Start a conversation - works for both PATIENT→DOCTOR and DOCTOR→PATIENT
     * POST /api/chat/conversations/start?doctorId=123 (Patient starting chat)
     * POST /api/chat/conversations/start?patientId=456 (Doctor starting chat)
     */
    @PostMapping("/conversations/start")
    public ResponseEntity<?> startConversation(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            Authentication authentication) {

        try {
            log.info("=== START CONVERSATION REQUEST ===");
            log.info("doctorId parameter: {}", doctorId);
            log.info("patientId parameter: {}", patientId);

            if (doctorId == null && patientId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Either doctorId or patientId is required"));
            }

            if (doctorId != null && patientId != null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Provide either doctorId OR patientId, not both"));
            }

            Long currentUserId = getCurrentUserId(authentication);
            log.info("Current authenticated user ID: {}", currentUserId);

            // Determine final patient and doctor IDs (Long everywhere)
            Long finalPatientId;
            Long finalDoctorId;

            if (doctorId != null) {
                // Current user is PATIENT, starting chat with DOCTOR
                finalPatientId = currentUserId;
                finalDoctorId = doctorId;
                log.info("Patient {} starting chat with Doctor {}", finalPatientId, finalDoctorId);
            } else {
                // Current user is DOCTOR, starting chat with PATIENT
                finalDoctorId = currentUserId;
                finalPatientId = patientId;
                log.info("Doctor {} starting chat with Patient {}", finalDoctorId, finalPatientId);
            }

            Conversation conversation = chatService.createConversation(finalPatientId, finalDoctorId);
            log.info("Conversation created/retrieved with ID: {}", conversation.getId());

            ConversationDTO dto = mapToConversationDTO(conversation, currentUserId);

            log.info("=== CONVERSATION START SUCCESS ===");
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            log.error("=== ERROR STARTING CONVERSATION ===", e);

            return ResponseEntity.status(500)
                    .body(Map.of(
                            "error", e.getMessage() != null ? e.getMessage() : "Unknown error",
                            "type", e.getClass().getSimpleName(),
                            "timestamp", LocalDateTime.now().toString()
                    ));
        }
    }

    // ========== HELPER METHODS ==========

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof com.example.mainservice.security.CustomUserDetails cud) {
            return cud.getId();
        }

        throw new RuntimeException("User not authenticated");
    }


    private ConversationDTO mapToConversationDTO(Conversation conv, Long currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conv.getId());
        dto.setLastMessage(conv.getLastMessage());
        dto.setTimestamp(conv.getTimestamp());

        Patient patient = patientRepo.findById(conv.getPatientId()).orElse(null);
        Doctor doctor = doctorRepo.findById(conv.getDoctorId()).orElse(null);

        if (patient != null) {
            dto.setPatient(new ConversationDTO.UserInfo(
                    patient.getId(),
                    patient.getName(),
                    null, // must match your field
                    false,
                    "PATIENT"
            ));
        }

        if (doctor != null) {
            dto.setDoctor(new ConversationDTO.UserInfo(
                    doctor.getId(),
                    doctor.getName(),
                    null,
                    false,
                    "DOCTOR" // OR doctor.getPosition()
            ));
        }

        dto.setUnreadCount(chatService.getUnreadCount(conv.getId(), currentUserId));
        dto.setOnline(false);

        return dto;
    }




    private ChatMessageDTO mapToChatMessageDTO(ChatMessage msg) {
        var attachments = (msg.getAttachments() == null) ? List.<ChatAttachmentDTO>of()
                : msg.getAttachments().stream().map(a ->
                ChatAttachmentDTO.builder()
                        .fileName(a.getFileName())
                        .url(a.getUrl())
                        .contentType(a.getContentType())
                        .size(a.getSize() != null ? a.getSize() : 0L)
                        .build()
        ).toList();

        return ChatMessageDTO.builder()
                .id(msg.getId())
                .conversationId(msg.getConversation().getId())
                .senderId(msg.getSenderId())
                .receiverId(msg.getReceiverId())
                .content(msg.getContent())
                .type(msg.getType())
                .timestamp(msg.getTimestamp())
                .read(msg.getRead())
                .attachments(attachments)
                .build();
    }
}
