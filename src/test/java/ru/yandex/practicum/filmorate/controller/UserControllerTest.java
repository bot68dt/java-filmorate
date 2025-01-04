package ru.yandex.practicum.filmorate.controller;

import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class UserControllerTest {

    public static UserController userController = new UserController();
    User user = User.of(Long.parseLong("0"), "name111", "name111@mail.ru", "name111@mail", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    static User user10 = User.of(Long.parseLong("0"), "name111", "name1113@mail.ru", "name111@mail", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    User user1 = User.of(Long.parseLong("0"), "name1", "name111mail.ru", "name111@mail", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    User user2 = User.of(Long.parseLong("0"), "name1", "name111@mail.ru", "name111@mail", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    User user3 = User.of(Long.parseLong("0"), "name1", "name161@mail.ru", " ", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    User user4 = User.of(Long.parseLong("0"), "name1", "name181@mail.ru", "name111@mail", LocalDate.parse("2028-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    User user5 = User.of(null, "name1", "name111@mail.ru", "name119@mail", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    User user6 = User.of(Long.parseLong("600"), "name1", "name911@mail.ru", "name111@mail", LocalDate.parse("2020-04-19", DateTimeFormatter.ofPattern("yyyy-MM-dd")));

    @BeforeAll
    public static void start() throws ConditionsNotMetException, DuplicatedDataException {
        userController.create(user10);
    }

    @Test
    public void testValidate() throws ConditionsNotMetException, DuplicatedDataException {
        assertEquals(userController.create(user), user);
    }

    @Test(expected = ConditionsNotMetException.class)
    public void testMail() throws ConditionsNotMetException, DuplicatedDataException {
        userController.create(user1);
    }

    @Test(expected = DuplicatedDataException.class)
    public void testDuplicate() throws ConditionsNotMetException, DuplicatedDataException {
        userController.create(user2);
    }

    @Test(expected = ConditionsNotMetException.class)
    public void testLogin() throws ConditionsNotMetException, DuplicatedDataException {
        userController.create(user3);
    }

    @Test(expected = ConditionsNotMetException.class)
    public void testBirthday() throws ConditionsNotMetException, DuplicatedDataException {
        userController.create(user4);
    }

    @Test(expected = ConditionsNotMetException.class)
    public void testNoId() throws ConditionsNotMetException, NotFoundException, DuplicatedDataException {
        userController.update(user5);
    }

    @Test(expected = NotFoundException.class)
    public void testWrongId() throws ConditionsNotMetException, NotFoundException, DuplicatedDataException {
        userController.update(user6);
    }

    @AfterAll
    public static void testGet() {
        assertNotNull(userController.findAll());
    }
}