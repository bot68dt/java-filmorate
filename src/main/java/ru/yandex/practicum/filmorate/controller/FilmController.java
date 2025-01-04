package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j

public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Обработка Get-запроса...");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) throws ConditionsNotMetException {
        log.info("Обработка Create-запроса...");
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Exception", new ConditionsNotMetException("Название не может быть пустым"));
            throw new ConditionsNotMetException("Название не может быть пустым");
        }
        if (film.getDescription().length() > 200 && film.getDescription().contains(" ")) {
            log.error("Exception", new ConditionsNotMetException("Максимальная длина описания — 200 символов"));
            throw new ConditionsNotMetException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(ChronoLocalDate.from(LocalDateTime.of(1895, 12, 28, 00, 00, 00)))) {
            log.error("Exception", new ConditionsNotMetException("Дата релиза — не раньше 28 декабря 1895 года"));
            throw new ConditionsNotMetException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0 || film.getDuration() == null) {
            log.error("Exception", new ConditionsNotMetException("Продолжительность фильма должна быть положительным числом"));
            throw new ConditionsNotMetException("Продолжительность фильма должна быть положительным числом");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    // вспомогательный метод для генерации идентификатора нового фильма
    private long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) throws ConditionsNotMetException, NotFoundException {
        log.info("Обработка Put-запроса...");
        if (newFilm.getId() == null) {
            log.error("Exception", new ConditionsNotMetException("Id должен быть указан"));
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() == null || newFilm.getName().isBlank()) {
                log.error("Exception", new ConditionsNotMetException("Название не может быть пустым"));
                throw new ConditionsNotMetException("Название не может быть пустым");
            } else oldFilm.setName(newFilm.getName());
            if (newFilm.getDescription().length() > 200 && newFilm.getDescription().contains(" ")) {
                log.error("Exception", new ConditionsNotMetException("Максимальная длина описания — 200 символов"));
                throw new ConditionsNotMetException("Максимальная длина описания — 200 символов");
            } else oldFilm.setName(newFilm.getDescription());
            if (newFilm.getReleaseDate().isBefore(ChronoLocalDate.from(LocalDateTime.of(1895, 12, 28, 00, 00, 00)))) {
                log.error("Exception", new ConditionsNotMetException("Дата релиза — не раньше 28 декабря 1895 года"));
                throw new ConditionsNotMetException("Дата релиза — не раньше 28 декабря 1895 года");
            } else oldFilm.setReleaseDate(newFilm.getReleaseDate());
            if (newFilm.getDuration() <= 0 || newFilm.getDuration() == null) {
                log.error("Exception", new ConditionsNotMetException("Продолжительность фильма должна быть положительным числом"));
                throw new ConditionsNotMetException("Продолжительность фильма должна быть положительным числом");
            } else oldFilm.setDuration(newFilm.getDuration());
            return oldFilm;
        } else {
            log.error("Exception", new ConditionsNotMetException("Фильм с id = " + newFilm.getId() + " не найден"));
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }
    }
}