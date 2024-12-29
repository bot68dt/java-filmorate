package ru.yandex.practicum.filmorate.exception;

import java.io.IOException;

public class ConditionsNotMetException extends IOException {
    public ConditionsNotMetException(final String message) {
        super(message);
    }

    @Override
    public Throwable fillInStackTrace() {
        return null;
    }
}