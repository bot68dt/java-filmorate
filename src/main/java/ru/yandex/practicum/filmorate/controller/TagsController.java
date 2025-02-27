package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.GenreConstant;
import ru.yandex.practicum.filmorate.model.MpaConstant;
import ru.yandex.practicum.filmorate.service.FilmInterface;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@RestController
//@RequestMapping("/films")

public class TagsController {
    @Autowired
    @Qualifier("FilmDbStorage")
    private final FilmStorage filmStorage;

    @Autowired
    @Qualifier("UserDbStorage")
    private final UserStorage userStorage;

    private final FilmInterface filmInterface;

    @Autowired
    public TagsController(FilmStorage filmStorage, UserStorage userStorage, FilmInterface filmInterface) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.filmInterface = filmInterface;
    }

    @GetMapping("/genres")
    public List<GenreConstant> viewGenre() throws NotFoundException {
        return filmInterface.viewGenre();
    }

    @GetMapping("/genres/{id}")
    public GenreConstant viewGenreName(@PathVariable("id") Long id) throws ConditionsNotMetException, NotFoundException {
        return filmInterface.viewGenreName(id);
    }

    @GetMapping("/mpa")
    public List<MpaConstant> viewRating() throws NotFoundException {
        return filmInterface.viewFilmsRating();
    }

    @GetMapping("/mpa/{id}")
    public MpaConstant viewRatingName(@PathVariable("id") Long id) throws ConditionsNotMetException, NotFoundException {
        return filmInterface.viewRatingName(id);
    }
}