package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.GenreConstant;
import ru.yandex.practicum.filmorate.model.MpaConstant;

import java.util.LinkedHashSet;
import java.util.List;

public interface FilmInterface {
    Film addLike(Long idUser, Long idFilm);

    Film delLike(Long idUser, Long idFilm);

    LinkedHashSet<Film> viewRating(Long count);

    List<GenreConstant> viewGenre() throws NotFoundException;

    GenreConstant viewGenreName(Long id) throws NotFoundException;

    List<MpaConstant> viewFilmsRating() throws NotFoundException;

    MpaConstant viewRatingName(Long id) throws NotFoundException;
}
