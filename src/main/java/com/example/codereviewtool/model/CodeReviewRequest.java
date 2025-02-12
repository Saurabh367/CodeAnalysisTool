package com.example.codereviewtool.model;

/**
 * Model for the code review request.
 */
public class CodeReviewRequest {
    private String code;

    /**
     * Default constructor.
     * <p>This empty constructor is required for serialization/deserialization.</p>
     */
    public CodeReviewRequest() {
        // Default constructor
    }

    public CodeReviewRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
