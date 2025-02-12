package com.example.codereviewtool.request;

public class CodeRequest {
    private String code;
    private String language;

    // Constructors
    public CodeRequest() {}

    public CodeRequest(String code, String language) {
        this.code = code;
        this.language = language;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
