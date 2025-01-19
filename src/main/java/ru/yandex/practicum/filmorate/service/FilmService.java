package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
    private final TreeMap<String, Integer> filmsWithLikes = new TreeMap();

    @Autowired
    public FilmService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @Override
    public boolean addLike(String idUser, String idFilm) throws ConditionsNotMetException {
        log.info("Обработка Post-запроса...");
        if (userStorage.findById(idUser) != null && filmStorage.findById(idFilm) != null) {
            if (!filmStorage.findById(idFilm).getLikesIds().add(Long.valueOf(idUser))) {
                log.error("Exception", new ConditionsNotMetException(idUser, "Пользователь с данным идентификатором уже оставлял лайк."));
                throw new ConditionsNotMetException(idUser, "Пользователь с данным идентификатором уже оставлял лайк.");
            }
            filmStorage.findById(idFilm).getLikesIds().add(Long.valueOf(idUser));
            filmsWithLikes.put(filmStorage.findById(idFilm).getName(), filmStorage.findById(idFilm).getLikesIds().size());
        }
        return true;
    }

    @Override
    public boolean delLike(String idUser, String idFilm) throws ConditionsNotMetException {
        log.info("Обработка Del-запроса...");
        if (userStorage.findById(idUser) != null && filmStorage.findById(idFilm) != null) {
            if (!filmStorage.findById(idFilm).getLikesIds().remove(Long.valueOf(idUser))) {
                log.error("Exception", new ConditionsNotMetException(idUser, "Пользователь с данным идентификатором не оставлял лайк."));
                throw new ConditionsNotMetException(idUser, "Пользователь с данным идентификатором не оставлял лайк.");
            }
            filmStorage.findById(idFilm).getLikesIds().remove(Long.valueOf(idUser));
            filmsWithLikes.put(filmStorage.findById(idFilm).getName(), filmStorage.findById(idFilm).getLikesIds().size());
        }
        return true;
    }

    @Override
    public Map<String, Integer> viewRaiting(String count) throws NotFoundException {
        log.info("Обработка Get-запроса...");
        if (filmsWithLikes.isEmpty()) {
            log.error("Exception", new NotFoundException(count, "Список фильмов с рейтингом пуст."));
            throw new NotFoundException(count, "Список фильмов с рейтингом пуст.");
        }
        Map<String, Integer> sorted;
        if (StringUtils.isNumeric(count)) {
            sorted = filmsWithLikes.entrySet().stream().limit(Long.valueOf(count)).sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        } else {
            sorted = filmsWithLikes.entrySet().stream().limit(10).sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        }
        return sorted;
    }
}