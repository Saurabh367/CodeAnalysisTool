package com.example.codereviewtool.service;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodeExecutionService {

    public String executeCode(String code) {
        try {
            // Extract class name from the code using regex (assuming it's the first class found)
            String className = extractClassName(code);
            if (className == null) {
                return "Error: No class name found in the provided code.";
            }

            // Save the Java code to a file with the correct name
            Path javaFile = Files.createTempFile(className, ".java");
            Path renamedFile = javaFile.resolveSibling(className + ".java");
            Files.write(renamedFile, code.getBytes(), StandardOpenOption.CREATE);

            // Compile the Java file
            Process compileProcess = new ProcessBuilder("javac", renamedFile.toString()).start();
            compileProcess.waitFor(5, TimeUnit.SECONDS);

            // Check if compilation was successful
            if (compileProcess.exitValue() != 0) {
                return "Compilation failed: " + new String(compileProcess.getErrorStream().readAllBytes());
            }

            // Run the compiled Java class
            Process runProcess = new ProcessBuilder("java", "-cp", javaFile.getParent().toString(), className).start();
            runProcess.waitFor(5, TimeUnit.SECONDS);

            // Get the output
            String output = new String(runProcess.getInputStream().readAllBytes());
            return "Execution Output: " + output;

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // Utility method to extract class name from the Java code using regex
    private String extractClassName(String code) {
        Pattern pattern = Pattern.compile("public class\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1); // Return the first found class name
        }
        return null; // No class name found
    }
}
