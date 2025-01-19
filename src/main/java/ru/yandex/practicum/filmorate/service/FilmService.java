package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "TRACE")
@ConfigurationPropertiesScan
public class FilmService implements FilmInterface {
    UserStorage userStorage;
    FilmStorage filmStorage;
    private final TreeMap<Film, Integer> filmsWithLikes = new TreeMap();

    @Autowired
    public FilmService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @Override
    public Film addLike(String idUser, String idFilm) throws ConditionsNotMetException {
        log.info("Обработка Post-запроса...");
        if (userStorage.findById(idUser) != null && filmStorage.findById(idFilm) != null) {
            if (!filmStorage.findById(idFilm).getLikedUsers().add(userStorage.findById(idUser))) {
                log.error("Exception", new ConditionsNotMetException(idUser, "Пользователь с данным идентификатором уже оставлял лайк."));
                throw new ConditionsNotMetException(idUser, "Пользователь с данным идентификатором уже оставлял лайк.");
            }
            filmStorage.findById(idFilm).getLikedUsers().add(userStorage.findById(idUser));
            int a = filmStorage.findById(idFilm).getLikedUsers().size();
            filmsWithLikes.put(filmStorage.findById(idFilm), a);
        }
        return filmStorage.findById(idFilm);
    }

    @Override
    public Film delLike(String idUser, String idFilm) throws ConditionsNotMetException {
        log.info("Обработка Del-запроса...");
        if (userStorage.findById(idUser) != null && filmStorage.findById(idFilm) != null) {
            if (!filmStorage.findById(idFilm).getLikedUsers().remove(userStorage.findById(idUser))) {
                log.error("Exception", new ConditionsNotMetException(idUser, "Пользователь с данным идентификатором не оставлял лайк."));
                throw new ConditionsNotMetException(idUser, "Пользователь с данным идентификатором не оставлял лайк.");
            }
            filmStorage.findById(idFilm).getLikedUsers().remove(userStorage.findById(idUser));
            filmsWithLikes.put(filmStorage.findById(idFilm), filmStorage.findById(idFilm).getLikedUsers().size());
        }
        return filmStorage.findById(idFilm);
    }

    @Override
    public Set<Film> viewRaiting(String count) throws NotFoundException {
        log.info("Обработка Get-запроса...");
        if (filmsWithLikes.isEmpty()) {
            log.error("Exception", new NotFoundException(count, "Список фильмов с рейтингом пуст."));
            throw new NotFoundException(count, "Список фильмов с рейтингом пуст.");
        }
        Map<Film, Integer> sorted;
        if (StringUtils.isNumeric(count)) {
            sorted = filmsWithLikes.entrySet().stream().limit(Long.valueOf(count)).sorted(Map.Entry.<Film, Integer>comparingByValue().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        } else {
            sorted = filmsWithLikes.entrySet().stream().limit(10).sorted(Map.Entry.<Film, Integer>comparingByValue().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        }
        return sorted.keySet();
    }
}