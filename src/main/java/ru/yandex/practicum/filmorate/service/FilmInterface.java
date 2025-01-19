package ru.yandex.practicum.filmorate.service;

import java.util.Set;

public interface FilmInterface {
    public boolean addLike(String idUser, String idFilm);

    public boolean delLike(String idUser, String idFilm);

    public Set<String> viewRaiting(String count);
}
