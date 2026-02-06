package com.example.mainservice.service;

import java.nio.file.Path;

public interface OcrExtractionService {
    String extractText(Path filePath, String contentType);
}
