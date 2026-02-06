package com.example.mainservice.service.impl;

import com.example.mainservice.service.OcrExtractionService;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

@Service
public class OcrExtractionServiceImpl implements OcrExtractionService {

    @Value("${ocr.tesseract.datapath}")
    private String tessDataPath;

    @Value("${ocr.tesseract.lang:eng}")
    private String lang;

    @Value("${ocr.maxChars:8000}")
    private int maxChars;

    @Override
    public String extractText(Path filePath, String contentType) {
        String lower = filePath.toString().toLowerCase();

        try {
            // 1) PDF: try normal text extraction first (fast)
            if (lower.endsWith(".pdf") || (contentType != null && contentType.contains("pdf"))) {
                String text = extractPdfText(filePath);
                if (hasMeaningfulText(text)) {
                    return trimToLimit(clean(text));
                }

                // If PDF has no text (scanned), do OCR per page
                return trimToLimit(clean(ocrPdfPages(filePath)));
            }

            // 2) Images: OCR directly
            if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".bmp")) {
                return trimToLimit(clean(ocrImage(filePath)));
            }

            // Other formats: return empty for now
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private String extractPdfText(Path pdfPath) throws Exception {
        try (PDDocument doc = PDDocument.load(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String ocrPdfPages(Path pdfPath) throws Exception {
        StringBuilder sb = new StringBuilder();

        try (PDDocument doc = PDDocument.load(pdfPath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(doc);

            ITesseract tesseract = buildTesseract();

            int pages = doc.getNumberOfPages();
            for (int i = 0; i < pages; i++) {
                // 300 DPI gives better OCR accuracy
                BufferedImage image = renderer.renderImageWithDPI(i, 300);
                String pageText = tesseract.doOCR(image);
                if (pageText != null) {
                    sb.append(pageText).append("\n\n");
                }
            }
        }
        return sb.toString();
    }

    private String ocrImage(Path imagePath) throws Exception {
        ITesseract tesseract = buildTesseract();
        return tesseract.doOCR(imagePath.toFile());
    }

    private ITesseract buildTesseract() {
        Tesseract t = new Tesseract();
        t.setDatapath(tessDataPath);
        t.setLanguage(lang);
        // Optional: improve reading for documents
        // t.setTessVariable("user_defined_dpi", "300");
        return t;
    }

    private boolean hasMeaningfulText(String t) {
        if (t == null) return false;
        // remove whitespace and check length
        String compact = t.replaceAll("\\s+", "");
        return compact.length() >= 30;
    }

    private String clean(String t) {
        if (t == null) return "";
        // Keep line breaks but remove extra spaces
        return t.replaceAll("[ \\t]+", " ").trim();
    }

    private String trimToLimit(String t) {
        if (t == null) return "";
        if (t.length() <= maxChars) return t;
        return t.substring(0, maxChars) + "\n...\n[TRUNCATED]";
    }
}
