package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
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
    private final TreeMap<Long, Integer> filmsWithLikes = new TreeMap();

    @Autowired
    public FilmService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @Override
    public Film addLike(Long idUser, Long idFilm) throws ConditionsNotMetException {
        log.info("Обработка Post-запроса...");
        if (userStorage.findById(idUser) != null && filmStorage.findById(idFilm) != null) {
            boolean a = filmStorage.findById(idFilm).getLikedUsers().add(userStorage.findById(idUser));
            if (!a) {
                log.error("Exception", new ConditionsNotMetException(idUser.toString(), "Пользователь с данным идентификатором уже оставлял лайк."));
                throw new ConditionsNotMetException(idUser.toString(), "Пользователь с данным идентификатором уже оставлял лайк.");
            }
            filmsWithLikes.put(filmStorage.findById(idFilm).getId(), filmStorage.findById(idFilm).getLikedUsers().size());
        }
        return filmStorage.findById(idFilm);
    }

    @Override
    public Film delLike(Long idUser, Long idFilm) throws ConditionsNotMetException {
        log.info("Обработка Del-запроса...");
        if (userStorage.findById(idUser) != null && filmStorage.findById(idFilm) != null) {
            boolean a = filmStorage.findById(idFilm).getLikedUsers().remove(userStorage.findById(idUser));
            if (!a) {
                log.error("Exception", new ConditionsNotMetException(idUser.toString(), "Пользователь с данным идентификатором не оставлял лайк."));
                throw new ConditionsNotMetException(idUser.toString(), "Пользователь с данным идентификатором не оставлял лайк.");
            }
            filmsWithLikes.put(filmStorage.findById(idFilm).getId(), filmStorage.findById(idFilm).getLikedUsers().size());
        }
        return filmStorage.findById(idFilm);
    }

    @Override
    public List<Film> viewRaiting(Long count) throws NotFoundException {
        log.info("Обработка Get-запроса...");
        if (filmsWithLikes.isEmpty()) {
            log.error("Exception", new NotFoundException(count.toString(), "Список фильмов с рейтингом пуст."));
            throw new NotFoundException(count.toString(), "Список фильмов с рейтингом пуст.");
        }
        Map<Long, Integer> sorted;
        if (count != 0 || !count.equals(null)) {
            sorted = filmsWithLikes.entrySet().stream().sorted(Map.Entry.<Long, Integer>comparingByValue().reversed()).limit(count).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        } else {
            sorted = filmsWithLikes.entrySet().stream().sorted(Map.Entry.<Long, Integer>comparingByValue().reversed()).limit(10).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        }
        List films = new LinkedList();
        for (Long l : sorted.keySet())
            films.add(filmStorage.findById(l));
        return films;
    }
}