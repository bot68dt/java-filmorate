package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmInterface {
    public Film addLike(Long idUser, Long idFilm);

    public Film delLike(Long idUser, Long idFilm);

    public List<String> viewRaiting(Long count);
}
