package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Buffer;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmInterface;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/films")

public class FilmController {
    @Autowired
    @Qualifier("FilmDbStorage")
    private final FilmStorage filmStorage;

    @Autowired
    @Qualifier("UserDbStorage")
    private final UserStorage userStorage;

    private final FilmInterface filmInterface;

    @Autowired
    public FilmController(FilmStorage filmStorage, UserStorage userStorage, FilmInterface filmInterface) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.filmInterface = filmInterface;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable("id") Long id) throws ConditionsNotMetException, NotFoundException {
        return filmStorage.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@Valid @RequestBody ObjectNode objectNode) throws ConditionsNotMetException, NullPointerException {
        String name = objectNode.get("name").asText();
        String description = objectNode.get("description").asText();
        String releaseDate = objectNode.get("releaseDate").asText();
        Integer duration = objectNode.get("duration").asInt();
        List<String> mpa = objectNode.get("mpa").findValuesAsText("id");
        List<String> genres = objectNode.get("genres").findValuesAsText("id");
        //return filmStorage.create(film);
        return filmStorage.create(Buffer.of(Long.valueOf(0), name, description, LocalDate.parse(releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")), duration, genres, Long.valueOf(mpa.get(0).toString())));
    }

    @PutMapping
    public Film update(@Valid @RequestBody ObjectNode objectNode) throws ConditionsNotMetException, NotFoundException {
        Long id = objectNode.get("id").asLong();
        String name = objectNode.get("name").asText();
        String description = objectNode.get("description").asText();
        String releaseDate = objectNode.get("releaseDate").asText();
        Integer duration = objectNode.get("duration").asInt();
        List<String> mpa = objectNode.get("mpa").findValuesAsText("id");
        List<String> genres = objectNode.get("genres").findValuesAsText("id");
        return filmStorage.update(Buffer.of(id, name, description, LocalDate.parse(releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")), duration, genres, Long.valueOf(mpa.get(0).toString())));
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@Valid @RequestBody @PathVariable("id") Long id, @PathVariable("userId") Long userId) throws ConditionsNotMetException {
        return filmInterface.addLike(userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film delLike(@Valid @RequestBody @PathVariable("id") Long id, @PathVariable("userId") Long userId) throws NotFoundException {
        return filmInterface.delLike(userId, id);
    }

    @GetMapping("/popular")
    public Map<String, Long> viewRaiting(@RequestParam(required = false) Long count) throws NotFoundException {
        return filmInterface.viewRating(count);
    }

    @GetMapping("/genres")
    public Map<Long, Set<Long>> viewGenre() throws NotFoundException {
        return filmInterface.viewGenre();
    }

    @GetMapping("/genres/{id}")
    public Map<Long, String> viewGenreName(@PathVariable("id") Long id) throws ConditionsNotMetException, NotFoundException {
        return filmInterface.viewGenreName(id);
    }

    @GetMapping("/mpa")
    public Map<Long, Long> viewRating() throws NotFoundException {
        return filmInterface.viewFilmsRating();
    }

    @GetMapping("/mpa/{id}")
    public Map<Long, String> viewRatingName(@PathVariable("id") Long id) throws ConditionsNotMetException, NotFoundException {
        return filmInterface.viewRatingName(id);
    }
}