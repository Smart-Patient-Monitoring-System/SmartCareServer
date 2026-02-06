package com.example.mainservice.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatAttachmentDTO {

    private String fileName;
    private String url;
    private String contentType;
    private long size;
}
