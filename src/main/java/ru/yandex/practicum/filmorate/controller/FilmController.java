package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmInterface;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("/films")

public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmInterface filmInterface;

    @Autowired
    public FilmController(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.filmInterface = new FilmService(userStorage, filmStorage);
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable("id") String id) throws ConditionsNotMetException, NotFoundException {
        return filmStorage.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@Valid @RequestBody Film film) throws ConditionsNotMetException, NullPointerException {
        return filmStorage.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) throws ConditionsNotMetException, NotFoundException {
        return filmStorage.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@Valid @RequestBody @PathVariable("id") String id, @PathVariable("userId") String userId) throws ConditionsNotMetException {
        return filmInterface.addLike(userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film delLike(@Valid @RequestBody @PathVariable("id") String id, @PathVariable("userId") String userId) throws NotFoundException {
        return filmInterface.delLike(userId, id);
    }

    @GetMapping("/popular")
    public Set<String> viewRaiting(@RequestParam(required = false) String count) throws NotFoundException {
        return filmInterface.viewRaiting(count);
    }
}