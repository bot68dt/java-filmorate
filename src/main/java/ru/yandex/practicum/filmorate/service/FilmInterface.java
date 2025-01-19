package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmInterface {
    public Film addLike(String idUser, String idFilm);

    public Film delLike(String idUser, String idFilm);

    public List<String> viewRaiting(String count);
}
