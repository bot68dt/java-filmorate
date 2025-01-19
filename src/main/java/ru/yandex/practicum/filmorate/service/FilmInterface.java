package ru.yandex.practicum.filmorate.service;

import java.util.Map;

public interface FilmInterface {
    public boolean addLike(String idUser, String idFilm);

    public boolean delLike(String idUser, String idFilm);

    public Map<String, Integer> viewRaiting(String count);
}
