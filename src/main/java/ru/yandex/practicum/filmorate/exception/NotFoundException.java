package ru.yandex.practicum.filmorate.exception;

import java.io.IOException;

public class NotFoundException extends IOException {
    public NotFoundException(final String message) {
        super(message);
    }

    @Override
    public Throwable fillInStackTrace() {
        return null;
    }
}