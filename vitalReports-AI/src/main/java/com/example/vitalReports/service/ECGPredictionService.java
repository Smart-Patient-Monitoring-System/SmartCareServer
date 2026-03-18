package com.example.vitalReports.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.vitalReports.domain.model.ECGPredictionResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ECGPredictionService {

    @Value("${ecg.python.path:python}")
    private String pythonPath;

    @Value("${ecg.script.path:ecg_predict.py}")
    private String scriptPath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Resolve the script path to an absolute path.
     */
    private String resolveScriptPath() {
        File scriptFile = new File(scriptPath);
        if (scriptFile.isAbsolute() && scriptFile.exists()) {
            return scriptFile.getAbsolutePath();
        }

        File cwdFile = new File(System.getProperty("user.dir"), scriptPath);
        if (cwdFile.exists()) {
            return cwdFile.getAbsolutePath();
        }

        String[] searchPaths = { ".", "..", System.getProperty("user.dir") };
        for (String base : searchPaths) {
            File f = new File(base, scriptPath);
            if (f.exists()) {
                return f.getAbsolutePath();
            }
        }

        return scriptPath;
    }

    private File getWorkingDirectory() {
        String resolved = resolveScriptPath();
        return new File(resolved).getParentFile();
    }

    /**
     * Analyze ECG from uploaded .dat + .hea files.
     */
    public ECGPredictionResponse analyzeFiles(MultipartFile datFile, MultipartFile heaFile) throws Exception {

        Path tempDir = Files.createTempDirectory("ecg_upload_");

        String heaName = heaFile.getOriginalFilename();
        String recordName = heaName.substring(0, heaName.lastIndexOf("."));

        Path datPath = tempDir.resolve(recordName + ".dat");
        Path heaPath = tempDir.resolve(recordName + ".hea");

        datFile.transferTo(datPath.toFile());
        heaFile.transferTo(heaPath.toFile());

        String recordPath = tempDir.resolve(recordName).toString();

        String absoluteScriptPath = resolveScriptPath();
        File workDir = getWorkingDirectory();

        System.out.println("[ECG] Script path: " + absoluteScriptPath);
        System.out.println("[ECG] Working dir: " + workDir);
        System.out.println("[ECG] Record path: " + recordPath);

        ProcessBuilder pb = new ProcessBuilder(pythonPath, absoluteScriptPath, "file", recordPath);
        pb.directory(workDir);
        pb.redirectErrorStream(false);

        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder errorOutput = new StringBuilder();
        String errorLine;
        while ((errorLine = errorReader.readLine()) != null) {
            errorOutput.append(errorLine).append("\n");
        }

        int exitCode = process.waitFor();

        System.out.println("[ECG] Exit code: " + exitCode);
        System.out.println("[ECG] Output: " + output);
        if (!errorOutput.isEmpty()) {
            System.out.println("[ECG] Stderr: " + errorOutput);
        }

        // Cleanup
        try {
            Files.deleteIfExists(datPath);
            Files.deleteIfExists(heaPath);
            Files.deleteIfExists(tempDir);
        } catch (Exception ignored) {
        }

        if (exitCode != 0 || output.isEmpty()) {
            throw new RuntimeException("ECG analysis failed (exit code " + exitCode + "): " + errorOutput);
        }

        return objectMapper.readValue(output.toString(), ECGPredictionResponse.class);
    }

    /**
     * Original mode: predict from a raw signal array (180 samples).
     */
    public ECGPredictionResponse predictECG(List<Double> signal) throws Exception {

        Map<String, Object> input = new HashMap<>();
        input.put("signal", signal);
        String jsonInput = objectMapper.writeValueAsString(input);

        String absoluteScriptPath = resolveScriptPath();
        File workDir = getWorkingDirectory();

        ProcessBuilder pb = new ProcessBuilder(pythonPath, absoluteScriptPath, "signal", jsonInput);
        pb.directory(workDir);
        pb.redirectErrorStream(false);

        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String resultLine = reader.readLine();

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder errorOutput = new StringBuilder();
        String errorLine;
        while ((errorLine = errorReader.readLine()) != null) {
            errorOutput.append(errorLine).append("\n");
        }

        int exitCode = process.waitFor();

        if (exitCode != 0 || resultLine == null) {
            throw new RuntimeException("ECG prediction failed (exit code " + exitCode + "): " + errorOutput);
        }

        return objectMapper.readValue(resultLine, ECGPredictionResponse.class);
    }
}
