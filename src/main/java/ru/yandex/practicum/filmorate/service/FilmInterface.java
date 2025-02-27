package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaConstant;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmInterface {
    public Film addLike(Long idUser, Long idFilm);

    public Film delLike(Long idUser, Long idFilm);

    public LinkedHashSet<Film> viewRating(Long count);

    Map<Long, Set<Long>> viewGenre() throws NotFoundException;

    Map<Long, String> viewGenreName(Long id) throws NotFoundException;

    List<MpaConstant> viewFilmsRating() throws NotFoundException;

    MpaConstant viewRatingName(Long id) throws NotFoundException;
}
