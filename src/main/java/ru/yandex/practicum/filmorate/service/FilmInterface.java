package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmRequest;
import ru.yandex.practicum.filmorate.model.GenreConstant;
import ru.yandex.practicum.filmorate.model.MpaConstant;

import java.util.LinkedHashSet;
import java.util.List;

public interface FilmInterface {
    FilmRequest addLike(Long idUser, Long idFilm);

    FilmRequest delLike(Long idUser, Long idFilm);

    LinkedHashSet<FilmRequest> viewRating(Long count);

    List<GenreConstant> viewGenre() throws NotFoundException;

    GenreConstant viewGenreName(Long id) throws NotFoundException;

    List<MpaConstant> viewFilmsRating() throws NotFoundException;

    MpaConstant viewRatingName(Long id) throws NotFoundException;
}
