package ru.yandex.practicum.filmorate.storage.film;

import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@NoArgsConstructor
@Slf4j(topic = "TRACE")
@ConfigurationPropertiesScan
public class InMemoryFilmStorage implements FilmStorage {

    private static final Map<Long, Film> films = new HashMap();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Collection<Film> findAll() {
        log.info("Обработка Get-запроса...");
        return films.values();
    }

    @Override
    public Film findById(String id) throws ConditionsNotMetException, NotFoundException {
        log.info("Обработка Get-запроса...");
        if (!id.isBlank() && StringUtils.isNumeric(id)) {
            Iterator var1 = films.values().iterator();

            Film f;
            do {
                if (!var1.hasNext()) {
                    log.error("Exception", new NotFoundException(id, "Идентификатор фильма отсутствует в базе"));
                    throw new NotFoundException(id, "Идентификатор фильма отсутствует в базе");
                }

                f = (Film) var1.next();
            } while (!f.getId().equals(Long.valueOf(id)));
            return f;
        } else {
            log.error("Exception", new ConditionsNotMetException(id, "Идентификатор фильма не может быть нулевой"));
            throw new ConditionsNotMetException(id, "Идентификатор фильма не может быть нулевой");
        }
    }

    @Override
    public Film create(@Valid Film film) throws ConditionsNotMetException, NullPointerException {
        log.info("Обработка Create-запроса...");
        if (film.getName() != null && !film.getName().isBlank()) {
            if (film.getDescription().length() > 200) {
                log.error("Exception", new ConditionsNotMetException(film.getDescription(), "Максимальная длина описания — 200 символов"));
                throw new ConditionsNotMetException(film.getDescription(), "Максимальная длина описания — 200 символов");
            } else if (film.getReleaseDate().isBefore(ChronoLocalDate.from(LocalDateTime.of(1895, 12, 28, 0, 0, 0)))) {
                log.error("Exception", new ConditionsNotMetException(film.getReleaseDate().format(this.formatter), "Дата релиза — не раньше 28 декабря 1895 года"));
                throw new ConditionsNotMetException(film.getReleaseDate().format(this.formatter), "Дата релиза — не раньше 28 декабря 1895 года");
            } else if (film.getDuration() != null && film.getDuration() != 0) {
                if (film.getDuration() < 0) {
                    log.error("Exception", new ConditionsNotMetException(film.getDuration().toString(), "Продолжительность фильма должна быть положительным числом"));
                    throw new ConditionsNotMetException(film.getDuration().toString(), "Продолжительность фильма должна быть положительным числом");
                } else {
                    film.setId(this.getNextId());
                    film.setLikedUsers(new HashSet<>());
                    films.put(film.getId(), film);
                    return film;
                }
            } else {
                log.error("Exception", new NullPointerException("Продолжительность фильма не может быть нулевой"));
                throw new NullPointerException("Продолжительность фильма не может быть нулевой");
            }
        } else {
            log.error("Exception", new ConditionsNotMetException(film.getName(), "Название не может быть пустым"));
            throw new ConditionsNotMetException(film.getName(), "Название не может быть пустым");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong((id) -> {
            return id;
        }).max().orElse(0L);
        return ++currentMaxId;
    }

    @Override
    public Film update(@Valid Film newFilm) throws ConditionsNotMetException, NotFoundException {
        log.info("Обработка Put-запроса...");
        if (newFilm.getId() == null) {
            log.error("Exception", new ConditionsNotMetException(newFilm.getId().toString(), "Id должен быть указан"));
            throw new ConditionsNotMetException(newFilm.getId().toString(), "Id должен быть указан");
        } else if (films.containsKey(newFilm.getId())) {
            Film oldFilm = (Film) films.get(newFilm.getId());
            if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
                oldFilm.setName(newFilm.getName());
                if (newFilm.getDescription().length() > 200) {
                    log.error("Exception", new ConditionsNotMetException(newFilm.getDescription(), "Максимальная длина описания — 200 символов"));
                    throw new ConditionsNotMetException(newFilm.getDescription(), "Максимальная длина описания — 200 символов");
                } else {
                    oldFilm.setDescription(newFilm.getDescription());
                    if (newFilm.getReleaseDate().isBefore(ChronoLocalDate.from(LocalDateTime.of(1895, 12, 28, 0, 0, 0)))) {
                        log.error("Exception", new ConditionsNotMetException(newFilm.getReleaseDate().format(this.formatter), "Дата релиза — не раньше 28 декабря 1895 года"));
                        throw new ConditionsNotMetException(newFilm.getReleaseDate().format(this.formatter), "Дата релиза — не раньше 28 декабря 1895 года");
                    } else {
                        oldFilm.setReleaseDate(newFilm.getReleaseDate());
                        if (newFilm.getDuration() != null && newFilm.getDuration() != 0) {
                            if (newFilm.getDuration() < 0) {
                                log.error("Exception", new ConditionsNotMetException(newFilm.getDuration().toString(), "Продолжительность фильма должна быть положительным числом"));
                                throw new ConditionsNotMetException(newFilm.getDuration().toString(), "Продолжительность фильма должна быть положительным числом");
                            } else {
                                oldFilm.setDuration(newFilm.getDuration());
                                return oldFilm;
                            }
                        } else {
                            log.error("Exception", new NullPointerException("Продолжительность фильма не может быть нулевой"));
                            throw new NullPointerException("Продолжительность фильма не может быть нулевой");
                        }
                    }
                }
            } else {
                log.error("Exception", new ConditionsNotMetException(newFilm.getName(), "Название не может быть пустым"));
                throw new ConditionsNotMetException(newFilm.getName(), "Название не может быть пустым");
            }
        } else {
            log.error("Exception", new ConditionsNotMetException(newFilm.getId().toString(), "Фильм с указанным id не найден"));
            throw new NotFoundException(newFilm.getId().toString(), "Фильм с указанным id не найден");
        }
    }
}