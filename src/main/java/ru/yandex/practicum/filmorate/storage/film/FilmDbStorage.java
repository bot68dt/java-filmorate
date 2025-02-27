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
import ru.yandex.practicum.filmorate.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
        return Film.builder().id(resultSet.getLong("id")).name(resultSet.getString("name")).description(resultSet.getString("description")).releaseDate(resultSet.getDate("releaseDate").toLocalDate()).duration(resultSet.getInt("duration")).build();
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

    public static class FilmGenreExtractor implements ResultSetExtractor<Map<Long, LinkedHashSet<Long>>> {
        @Override
        public Map<Long, LinkedHashSet<Long>> extractData(ResultSet rs) throws SQLException {
            Map<Long, LinkedHashSet<Long>> data = new LinkedHashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("filmId");
                data.putIfAbsent(filmId, new LinkedHashSet<>());
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
    public List<Film> findAll() {
        log.info("Обработка Get-запроса...");
        String sqlQuery1 = "select id, name, description, releaseDate, duration from film";
        List<Film> films = jdbcTemplate.query(sqlQuery1, this::mapRowToFilm);
        String sqlQuery2 = "select filmId, userId from likedUsers";
        Map<Long, Set<Long>> likedUsers = jdbcTemplate.query(sqlQuery2, new LikedUsersExtractor());
        String sqlQuery3 = "select filmId, genreId from filmGenre";
        Map<Long, LinkedHashSet<Long>> filmGenre = jdbcTemplate.query(sqlQuery3, new FilmGenreExtractor());
        sqlQuery3 = "select id, ratingId from film";
        Map<Long, Long> filmRating = jdbcTemplate.query(sqlQuery3, new FilmRatingExtractor());
        for (Film film : films) {
            film.setLikedUsers(likedUsers.get(film.getId()));
            //LinkedHashSet<Long> genres = new LinkedHashSet<>();
            //for (Long g : filmGenre.get(film.getId()))
            //    genres.add(Genre.of(g));
            film.setGenres(filmGenre.get(film.getId()));
            film.setMpa(filmRating.get(film.getId()));
        }
        return films;
    }

    @Override
    public FilmRequest findById(Long id) throws ConditionsNotMetException, NotFoundException {
        log.info("Обработка Get-запроса...");
        if (id != 0 || !id.equals(null)) {
            try {
                jdbcTemplate.queryForObject("select id, name, description, releaseDate, duration from film where id = ?", this::mapRowToFilm, id);
            } catch (DataAccessException e) {
                if (e != null) {
                    log.error("Exception", new NotFoundException(id.toString(), "Идентификатор фильма отсутствует в базе"));
                    throw new NotFoundException(id.toString(), "Идентификатор фильма отсутствует в базе");
                }
            }
            Film film = jdbcTemplate.queryForObject("select id, name, description, releaseDate, duration from film where id = ?", this::mapRowToFilm, id);
            String sqlQuery2 = "select filmId, userId from likedUsers where filmId = ?";
            Map<Long, Set<Long>> likedUsers = jdbcTemplate.query(sqlQuery2, new LikedUsersExtractor(), id);
            String sqlQuery3 = "select filmId, genreId from filmGenre where filmId = ?";
            Map<Long, LinkedHashSet<Long>> filmGenre = jdbcTemplate.query(sqlQuery3, new FilmGenreExtractor(), id);
            sqlQuery3 = "select id, ratingId from film where id = ?";
            Map<Long, Long> filmRating = jdbcTemplate.query(sqlQuery3, new FilmRatingExtractor(), id);
            film.setLikedUsers(likedUsers.get(id));
            film.setGenres(filmGenre.get(id));
            LinkedHashSet<Genre> genres = new LinkedHashSet<>();
            if (!filmGenre.isEmpty()) {
                for (Long g : filmGenre.get(id))
                    genres.add(Genre.of(g));
            }
            film.setMpa(filmRating.get(id));
            return FilmRequest.of(film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), new HashSet<>(), Mpa.of(film.getMpa()),genres);
        } else {
            log.error("Exception", new ConditionsNotMetException(id.toString(), "Идентификатор фильма не может быть нулевой"));
            throw new ConditionsNotMetException(id.toString(), "Идентификатор фильма не может быть нулевой");
        }
    }

    @Override
    public FilmRequest create(@Valid Buffer buffer) throws ConditionsNotMetException, NullPointerException {
        log.info("Обработка Create-запроса...");
        if (buffer.getName() != null && !buffer.getName().isBlank() && !buffer.getName().equals("")) {
            if (buffer.getDescription().length() > 200) {
                log.error("Exception", new ConditionsNotMetException(buffer.getDescription(), "Максимальная длина описания — 200 символов"));
                throw new ConditionsNotMetException(buffer.getDescription(), "Максимальная длина описания — 200 символов");
            } else if (buffer.getReleaseDate().isBefore(ChronoLocalDate.from(LocalDateTime.of(1895, 12, 28, 0, 0, 0)))) {
                log.error("Exception", new ConditionsNotMetException(buffer.getReleaseDate().format(this.formatter), "Дата релиза — не раньше 28 декабря 1895 года"));
                throw new ConditionsNotMetException(buffer.getReleaseDate().format(this.formatter), "Дата релиза — не раньше 28 декабря 1895 года");
            } else if (buffer.getDuration() != null && buffer.getDuration() != 0) {
                if (buffer.getDuration() < 0) {
                    log.error("Exception", new ConditionsNotMetException(buffer.getDuration().toString(), "Продолжительность фильма должна быть положительным числом"));
                    throw new ConditionsNotMetException(buffer.getDuration().toString(), "Продолжительность фильма должна быть положительным числом");
                } else if (!(buffer.getMpa() > 0 && buffer.getMpa() < 6)) {
                    log.error("Exception", new NotFoundException(buffer.getMpa().toString(), "Некорректный рейтинг"));
                    throw new NotFoundException(buffer.getMpa().toString(), "Некорректный рейтинг");
                } else {
                    List<Long> genres;
                    LinkedHashSet<Genre> genres1 = new LinkedHashSet<>();
                    String sqlQuery;
                    SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("film").usingGeneratedKeyColumns("id");
                    Long f = simpleJdbcInsert.executeAndReturnKey(buffer.toMapBuffer()).longValue();
                    if (!buffer.getGenres().equals(List.of("нет жанра"))) {
                        genres = buffer.getGenres().stream().map(item -> Long.parseLong(item)).collect(Collectors.toList());
                        for (Long g : genres) {
                            if (!(g > 0 && g < 7)) {
                                log.error("Exception", new NotFoundException(g.toString(), "Некорректный жанр"));
                                throw new NotFoundException(g.toString(), "Некорректный жанр");
                            }
                        }
                        sqlQuery = "insert into filmGenre(filmId, genreId) " + "values (?, ?)";
                        for (Long g : genres) {
                            jdbcTemplate.update(sqlQuery, f, g);
                            genres1.add(Genre.of(g));
                        }
                    }
                    sqlQuery = "update film set " + "ratingId = ? " + "where id = ?";
                    jdbcTemplate.update(sqlQuery, buffer.getMpa(), f);

                    FilmRequest film = FilmRequest.of(f, buffer.getName(), buffer.getDescription(), buffer.getReleaseDate(), buffer.getDuration(), new HashSet<>(), Mpa.of(buffer.getMpa()), genres1);
                    return film;
                }
            } else {
                log.error("Exception", new NullPointerException("Продолжительность фильма не может быть нулевой"));
                throw new NullPointerException("Продолжительность фильма не может быть нулевой");
            }
        } else {
            log.error("Exception", new ConditionsNotMetException("NULL", "Название не может быть пустым"));
            throw new ConditionsNotMetException("NULL", "Название не может быть пустым");
        }
    }

    @Override
    public FilmRequest update(@Valid Buffer newFilm) throws ConditionsNotMetException, NotFoundException {
        log.info("Обработка Put-запроса...");
        if (newFilm.getId() == null) {
            log.error("Exception", new ConditionsNotMetException("NULL", "Id должен быть указан"));
            throw new ConditionsNotMetException("NULL", "Id должен быть указан");
        } else {
            FilmRequest oldFilm = findById(newFilm.getId());
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
                                if (!(newFilm.getMpa() > 0 && newFilm.getMpa() < 6)) {
                                    log.error("Exception", new NotFoundException(newFilm.getMpa().toString(), "Некорректный рейтинг"));
                                    throw new NotFoundException(newFilm.getMpa().toString(), "Некорректный рейтинг");
                                }
                                LinkedHashSet<Genre> genres1 = new LinkedHashSet<>();
                                if (!newFilm.getGenres().equals(List.of("нет жанра"))) {
                                    List<Long> genres = newFilm.getGenres().stream().map(item -> Long.parseLong(item)).collect(Collectors.toList());
                                    for (Long g : genres) {
                                        if (!(g > 0 && g < 7)) {
                                            log.error("Exception", new NotFoundException(g.toString(), "Некорректный жанр"));
                                            throw new NotFoundException(g.toString(), "Некорректный жанр");
                                        }
                                    }
                                    String sqlQuery = "delete from filmGenre where filmId = ?";
                                    jdbcTemplate.update(sqlQuery, oldFilm.getId());
                                    sqlQuery = "insert into filmGenre(filmId, genreId) " + "values (?, ?)";
                                    for (Long g : genres) {
                                        jdbcTemplate.update(sqlQuery, oldFilm.getId(), g);
                                        genres1.add(Genre.of(g));
                                    }
                                }
                                if (!oldFilm.getMpa().equals(newFilm.getMpa()) && newFilm.getMpa() > 0 && newFilm.getMpa() < 6)
                                    oldFilm.setMpa(Mpa.of(newFilm.getMpa()));
                                String sqlQuery500 = "update film set " + "name = ?, description = ?, releaseDate = ?, duration = ?, ratingId = ? " + "where id = ?";
                                jdbcTemplate.update(sqlQuery500, oldFilm.getName(), oldFilm.getDescription(), oldFilm.getReleaseDate(), oldFilm.getDuration(), oldFilm.getMpa().getId(), oldFilm.getId());
                                return FilmRequest.of(oldFilm.getId(), oldFilm.getName(), oldFilm.getDescription(), oldFilm.getReleaseDate(), oldFilm.getDuration(), new HashSet<>(), oldFilm.getMpa(),genres1);
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