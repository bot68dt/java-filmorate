package ru.yandex.practicum.filmorate.exception;

public class ImageFileException extends RuntimeException {
    private String parameter;
    private String reason;

    public ImageFileException(String parameter, String reason) {
        this.parameter = parameter;
        this.reason = reason;
    }

    public String getParameter() {
        return this.parameter;
    }

    public String getReason() {
        return this.reason;
    }

    public Throwable fillInStackTrace() {
        return null;
    }
}