package ru.yandex.practicum.filmorate.exception;

import java.io.IOException;

public class DuplicatedDataException extends IOException {
    public DuplicatedDataException(final String message) {
        super(message);
    }

    @Override
    public Throwable fillInStackTrace() {
        return null;
    }
}