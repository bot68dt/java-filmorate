package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Map;
import java.util.Set;

public interface FilmInterface {
    public Film addLike(Long idUser, Long idFilm);

    public Film delLike(Long idUser, Long idFilm);

    public Map<String, Long> viewRating(Long count);

    Map<Long, Set<Long>> viewGenre() throws NotFoundException;

    Map<Long, String> viewGenreName(Long id) throws NotFoundException;

    Map<Long, Long> viewFilmsRating() throws NotFoundException;

    Map<Long, String> viewRatingName(Long id) throws NotFoundException;
}
