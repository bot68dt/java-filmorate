package ru.yandex.practicum.filmorate.controller;

import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class FilmControllerTest {

    public static FilmController filmController = new FilmController();
    Film film = Film.of(Long.parseLong("0"), "name", "description", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")), 100);
    Film film1 = Film.of(Long.parseLong("0"), " ", "description", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")), 100);
    Film film2 = Film.of(Long.parseLong("0"), "name", "descriptiondescriptiondescriptiondescriptiondescriptiondescriptiondзззжescription" + "descriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondesc" + "riptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescriptiondescr" + "iptiondescriptiondescription", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")), 100);
    Film film3 = Film.of(Long.parseLong("0"), "name", "description", LocalDate.parse("1880-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")), 100);
    Film film4 = Film.of(Long.parseLong("0"), "name", "description", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")), -100);
    static Film film5 = Film.of(Long.parseLong("0"), "name111", "description", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")), 100);
    Film film6 = Film.of(null, "name", "description", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")), 100);
    Film film7 = Film.of(Long.parseLong("50"), "name", "description", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")), 100);

    @BeforeAll
    public static void start() throws ConditionsNotMetException {
        filmController.create(film5);
    }

    @Test
    public void testValidate() throws ConditionsNotMetException {
        assertEquals(filmController.create(film), film);
    }

    @Test(expected = ConditionsNotMetException.class)
    public void testName() throws ConditionsNotMetException {
        filmController.create(film1);
    }

    @Test(expected = ConditionsNotMetException.class)
    public void testDescription() throws ConditionsNotMetException {
        filmController.create(film2);
    }

    @Test(expected = ConditionsNotMetException.class)
    public void testReleaseDate() throws ConditionsNotMetException {
        filmController.create(film3);
    }

    @Test(expected = ConditionsNotMetException.class)
    public void testDuration() throws ConditionsNotMetException {
        filmController.create(film4);
    }

    @Test(expected = ConditionsNotMetException.class)
    public void testNoId() throws ConditionsNotMetException, NotFoundException {
        filmController.update(film6);
    }

    @Test(expected = NotFoundException.class)
    public void testWrongId() throws ConditionsNotMetException, NotFoundException {
        filmController.update(film7);
    }

    @AfterAll
    public static void testGet() {
        assertNotNull(filmController.findAll());
    }
}