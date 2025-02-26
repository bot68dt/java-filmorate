package ru.yandex.practicum.filmorate.storage.film;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j(topic = "TRACE")
@ConfigurationPropertiesScan
@Component
@Qualifier("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final JdbcTemplate jdbcTemplate;

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder().id(resultSet.getLong("id")).name(resultSet.getString("name")).description(resultSet.getString("description")).releaseDate(resultSet.getDate("releaseDate").toLocalDate()).duration(resultSet.getInt("duration")).likedUsers(new HashSet<>()).genres(resultSet.getObject("genres", Map.class)).mpa(resultSet.getObject("mpa", Map.class)).build();
    }

    public static class LikedUsersExtractor implements ResultSetExtractor<Map<Long, Set<Long>>> {
        @Override
        public Map<Long, Set<Long>> extractData(ResultSet rs) throws SQLException {
            Map<Long, Set<Long>> data = new LinkedHashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("filmId");
                data.putIfAbsent(filmId, new HashSet<>());
                Long userId = rs.getLong("userId");
                data.get(filmId).add(userId);
            }
            return data;
        }
    }

    public static class FilmGenreExtractor implements ResultSetExtractor<Map<Long, Set<Long>>> {
        @Override
        public Map<Long, Set<Long>> extractData(ResultSet rs) throws SQLException {
            Map<Long, Set<Long>> data = new LinkedHashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("filmId");
                data.putIfAbsent(filmId, new HashSet<>());
                Long genreId = rs.getLong("genreId");
                data.get(filmId).add(genreId);
            }
            return data;
        }
    }

    public static class FilmRatingExtractor implements ResultSetExtractor<Map<Long, Long>> {
        @Override
        public Map<Long, Long> extractData(ResultSet rs) throws SQLException {
            Map<Long, Long> data = new HashMap<>();
            while (rs.next()) {
                Long id = rs.getLong("id");
                data.putIfAbsent(id, Long.valueOf(0));
                Long ratingId = rs.getLong("ratingId");
                data.put(id, ratingId);
            }
            return data;
        }
    }

    @Override
    public Collection<Film> findAll() {
        log.info("Обработка Get-запроса...");
        String sqlQuery1 = "select id, name, description, releaseDate, duration, ratingId from film";
        Collection<Film> films = jdbcTemplate.query(sqlQuery1, this::mapRowToFilm);
        String sqlQuery2 = "select filmId, userId from likedUsers";
        Map<Long, Set<Long>> likedUsers = jdbcTemplate.query(sqlQuery2, new LikedUsersExtractor());
        String sqlQuery3 = "select filmId, genreId from filmGenre";
        Map<Long, Set<Long>> filmGenre = jdbcTemplate.query(sqlQuery3, new FilmGenreExtractor());
        sqlQuery3 = "select id, ratingId from film";
        Map<Long, Long> filmRating = jdbcTemplate.query(sqlQuery3, new FilmRatingExtractor());
        for (Film film : films) {
            film.setLikedUsers(likedUsers.get(film.getId()));
            film.setGenres(Map.of("id", filmGenre.get(film.getId())));
            film.setMpa(Map.of("id", filmRating.get(film.getId())));
        }
        return films;
    }

    @Override
    public Film findById(Long id) throws ConditionsNotMetException, NotFoundException {
        log.info("Обработка Get-запроса...");
        if (id != 0 || !id.equals(null)) {
            try {
                jdbcTemplate.queryForObject("select id, name, description, releaseDate, duration, ratingId from film where id = ?", this::mapRowToFilm, id);
            } catch (DataAccessException e) {
                if (e != null) {
                    log.error("Exception", new NotFoundException(id.toString(), "Идентификатор фильма отсутствует в базе"));
                    throw new NotFoundException(id.toString(), "Идентификатор фильма отсутствует в базе");
                }
            }
            Film film = jdbcTemplate.queryForObject("select id, name, description, releaseDate, duration, ratingId from film where id = ?", this::mapRowToFilm, id);
            String sqlQuery2 = "select filmId, userId from likedUsers where filmId = ?";
            Map<Long, Set<Long>> likedUsers = jdbcTemplate.query(sqlQuery2, new LikedUsersExtractor(), id);
            String sqlQuery3 = "select filmId, genreId from filmGenre where filmId = ?";
            Map<Long, Set<Long>> filmGenre = jdbcTemplate.query(sqlQuery3, new FilmGenreExtractor(), id);
            sqlQuery3 = "select id, ratingId from film where filmId = ?";
            Map<Long, Long> filmRating = jdbcTemplate.query(sqlQuery3, new FilmRatingExtractor(), id);
            film.setLikedUsers(likedUsers.get(id));
            film.setGenres(Map.of("id", filmGenre.get(id)));
            film.setMpa(Map.of("id", filmRating.get(id)));
            return film;
        } else {
            log.error("Exception", new ConditionsNotMetException(id.toString(), "Идентификатор фильма не может быть нулевой"));
            throw new ConditionsNotMetException(id.toString(), "Идентификатор фильма не может быть нулевой");
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
                    SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("film").usingGeneratedKeyColumns("id");
                    Long f = simpleJdbcInsert.executeAndReturnKey(film.toMapFilm()).longValue();
                    String sqlQuery = "insert into filmGenre(filmId, genreId) " + "values (?, ?)";
                    for (Long l : film.getGenres().get("id")) {
                        jdbcTemplate.update(sqlQuery, f, l);
                    }
                    sqlQuery = "update film set " + "ratingId = ? " + "where id = ?";
                    jdbcTemplate.update(sqlQuery, film.getMpa().get("id"), f);
                    return findById(f);
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

    @Override
    public Film update(@Valid Film newFilm) throws ConditionsNotMetException, NotFoundException {
        log.info("Обработка Put-запроса...");
        if (newFilm.getId() == null) {
            log.error("Exception", new ConditionsNotMetException("NULL", "Id должен быть указан"));
            throw new ConditionsNotMetException("NULL", "Id должен быть указан");
        } else {
            try {
                jdbcTemplate.queryForObject("select id, name, description, releaseDate, duration, ratingId from film where id = ?", this::mapRowToFilm, newFilm.getId());
            } catch (DataAccessException e) {
                if (e != null) {
                    log.error("Exception", new ConditionsNotMetException(newFilm.getId().toString(), "Идентификатор фильма отсутствует в базе"));
                    throw new ConditionsNotMetException(newFilm.getId().toString(), "Идентификатор фильма отсутствует в базе");
                }
            }
            Film oldFilm = jdbcTemplate.queryForObject("select id, name, description, releaseDate, duration, ratingId from film where id = ?", this::mapRowToFilm, newFilm.getId());
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
                                if (oldFilm.getMpa().get("id") != newFilm.getMpa().get("id") && newFilm.getMpa().get("id") > 0 && newFilm.getMpa().get("id") < 6)
                                    oldFilm.setMpa(newFilm.getMpa());
                                //oldFilm.setRatingId(newFilm.getRatingId());
                                String sqlQuery = "delete from filmGenre where filmId = ?";
                                jdbcTemplate.update(sqlQuery, newFilm.getId());
                                String sqlQuery500 = "update film set " + "name = ?, description = ?, releaseDate = ?, duration = ?, ratingId = ? " + "where id = ?";
                                jdbcTemplate.update(sqlQuery500, oldFilm.getName(), oldFilm.getDescription(), oldFilm.getReleaseDate(), oldFilm.getDuration(), oldFilm.getMpa().get("id"), oldFilm.getId());
                                String sqlQuery501 = "insert into filmGenre(filmId, genreId) " + "values (?, ?)";
                                for (Long l : oldFilm.getGenres().get("id")) {
                                    jdbcTemplate.update(sqlQuery501, oldFilm.getId(), l);
                                }
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
        }
    }
}