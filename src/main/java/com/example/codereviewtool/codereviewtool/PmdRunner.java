package com.example.codereviewtool.codereviewtool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for running PMD analysis.
 */
public final class PmdRunner {  // ✅ Use 'final'
    private PmdRunner() {
        // Prevent instantiation
    }

    /**
     * Runs PMD on the provided Java file and returns the output (JSON format).
     * Ensure that PMD is installed and available in your system's PATH if you're invoking it directly.
     *
     * @param filePath Path to the Java file.
     * @return PMD analysis results as a JSON string.
     */
    public static String runPmd(String filePath) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "pmd",
                    "-d", filePath,
                    "-R", "rulesets/java/quickstart.xml",
                    "-f", "json"
            );

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }

                // Capture errors
                StringBuilder errorOutput = new StringBuilder();
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line);
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    return "{\"error\": \"PMD execution failed: " + errorOutput.toString() + "\"}";
                }
            }
        } catch (Exception e) {
            return "{\"error\": \"PMD execution failed: " + e.getMessage() + "\"}";
        }
        return output.toString();
    }
}
