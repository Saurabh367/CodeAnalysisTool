package com.example.codereviewtool.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodeExecutionService {

    public String executeCode(String code, String language) {
        try {
            if (language.equalsIgnoreCase("java")) {
                return executeJavaCode(code);
            } else {
                // Determine file extension for non-Java languages
                String extension = getFileExtension(language);
                if (extension == null) {
                    return "Error: Unsupported language. Please provide a valid language.";
                }
                // Create a temporary file with the correct extension
                Path tempFile = Files.createTempFile("script", extension);
                Files.write(tempFile, code.getBytes(), StandardOpenOption.CREATE);

                // Get the execution command for non-Java languages
                String[] command = getExecutionCommand(language, tempFile.toString());
                if (command == null) {
                    return "Error: Could not determine execution command.";
                }

                // Execute the script
                Process runProcess = new ProcessBuilder(command).start();
                runProcess.waitFor(5, TimeUnit.SECONDS);

                // Capture the output and error output
                String output = new String(runProcess.getInputStream().readAllBytes());
                String errorOutput = new String(runProcess.getErrorStream().readAllBytes());

                // Cleanup the temporary file
                Files.deleteIfExists(tempFile);

                // Return execution result
                return errorOutput.isEmpty() ? "Execution Output: " + output : "Error: " + errorOutput;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Executes Java code dynamically.
     * This method extracts the public class name from the code, creates a temporary directory,
     * writes the code to a file named <ClassName>.java, compiles it, runs it, and returns the output.
     */
    private String executeJavaCode(String code) throws IOException, InterruptedException {
        // Extract the public class name from the code
        String className = extractClassNameFromCode(code);
        if (className == null) {
            return "Error: No class name found in the provided code.";
        }

        // Create a temporary directory to hold our Java source and class files
        Path tempDir = Files.createTempDirectory("javaexec");
        // Create the file with the proper name: e.g., Greeting.java or CatalogHierarchyService.java
        Path javaFile = tempDir.resolve(className + ".java");
        Files.write(javaFile, code.getBytes(), StandardOpenOption.CREATE);

        // Compile the Java file using javac, setting the working directory to the tempDir
        Process compileProcess = new ProcessBuilder("javac", javaFile.toString())
                .directory(tempDir.toFile())
                .start();
        compileProcess.waitFor(5, TimeUnit.SECONDS);

        // If compilation fails, capture and return the error output
        if (compileProcess.exitValue() != 0) {
            String errorMsg = new String(compileProcess.getErrorStream().readAllBytes());
            // Clean up temp directory
            deleteDirectoryRecursively(tempDir);
            return "Compilation failed: " + errorMsg;
        }

        // Run the compiled Java class, using the tempDir as the classpath
        Process runProcess = new ProcessBuilder("java", "-cp", tempDir.toString(), className)
                .directory(tempDir.toFile())
                .start();
        runProcess.waitFor(5, TimeUnit.SECONDS);

        // Capture the output from execution
        String output = new String(runProcess.getInputStream().readAllBytes());
        // Clean up the temporary directory recursively
        deleteDirectoryRecursively(tempDir);

        return "Execution Output: " + output;
    }

    private String getFileExtension(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> ".java";
            case "python" -> ".py";
            case "javascript" -> ".js";
            case "c" -> ".c";
            case "cpp", "c++" -> ".cpp";
            case "ruby" -> ".rb";
            case "go" -> ".go";
            default -> null;
        };
    }

    private String[] getExecutionCommand(String language, String filePath) {
        return switch (language.toLowerCase()) {
            case "java" -> new String[]{"sh", "-c", "javac " + filePath + " && java -cp " + new File(filePath).getParent() + " " + extractClassNameFromFileName(filePath)};
            case "python" -> new String[]{"python3", filePath};
            case "javascript" -> new String[]{"node", filePath};
            case "c" -> new String[]{"sh", "-c", "gcc " + filePath + " -o temp.out && ./temp.out"};
            case "cpp", "c++" -> new String[]{"sh", "-c", "g++ " + filePath + " -o temp.out && ./temp.out"};
            case "ruby" -> new String[]{"ruby", filePath};
            case "go" -> new String[]{"go", "run", filePath};
            default -> null;
        };
    }

    private String extractClassNameFromCode(String code) {
        // This regex matches a public class declaration and extracts the class name.
        Pattern pattern = Pattern.compile("public\\s+class\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractClassNameFromFileName(String filePath) {
        String fileName = new File(filePath).getName();
        return fileName.replace(".java", "");
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        Files.walk(path)
                .sorted((a, b) -> b.compareTo(a)) // delete children first
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        // Log or handle deletion exception if needed
                    }
                });
    }
}
