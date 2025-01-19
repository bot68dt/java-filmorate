package ru.yandex.practicum.filmorate.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.ImageFileException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@RestControllerAdvice
public class ErrorHandler {
    public ErrorHandler() {
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleConditions(final ConditionsNotMetException e) {
        return Map.of(e.getParameter(), e.getReason());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicatedData(final DuplicatedDataException e) {
        return Map.of(e.getParameter(), e.getReason());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(final NotFoundException e) {
        return Map.of(e.getParameter(), e.getReason());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleImageFile(final ImageFileException e) {
        return Map.of(e.getParameter(), e.getReason());
    }
}