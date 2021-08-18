package ru.javawebinar.topjava.util.exception;

public enum ErrorType {
    APP_ERROR("error.application"),
    DATA_NOT_FOUND("error.datanotfound"),
    DATA_ERROR("error.data"),
    VALIDATION_ERROR("error.validation");

    private String description;



    ErrorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}