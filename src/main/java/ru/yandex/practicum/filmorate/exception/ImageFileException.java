package ru.yandex.practicum.filmorate.exception;

import java.io.IOException;

public class ImageFileException extends IOException {
    public ImageFileException(final String message) {
        super(message);
    }

    @Override
    public Throwable fillInStackTrace() {
        return null;
    }
}