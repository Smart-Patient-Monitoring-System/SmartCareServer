package com.example.mainservice.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.file.Path;

@Component
public class ReportTextExtractor {

    public String extractText(Path filePath, String contentType) {
        try {
            String p = filePath.toString().toLowerCase();

            // PDF
            if (p.endsWith(".pdf") || (contentType != null && contentType.contains("pdf"))) {
                try (PDDocument doc = PDDocument.load(filePath.toFile())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    String text = stripper.getText(doc);
                    return clean(text);
                }
            }

            // DOCX
            if (p.endsWith(".docx") || (contentType != null && contentType.contains("wordprocessingml"))) {
                try (FileInputStream fis = new FileInputStream(filePath.toFile());
                     XWPFDocument document = new XWPFDocument(fis);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    return clean(extractor.getText());
                }
            }

            // Other formats: return empty for now
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private String clean(String t) {
        if (t == null) return "";
        t = t.replaceAll("\\s+", " ").trim();
        // limit huge texts (optional)
        if (t.length() > 5000) t = t.substring(0, 5000) + " ...";
        return t;
    }
}
