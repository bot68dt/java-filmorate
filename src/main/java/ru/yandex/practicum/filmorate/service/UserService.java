package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserService {

    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        log.info("Обработка Get-запроса...");
        return users.values();
    }

    public User create(@Valid User user) throws ConditionsNotMetException, DuplicatedDataException {
        log.info("Обработка Create-запроса...");
        duplicateCheck(user);
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@") || user.getEmail().contains(" ") || user.getEmail().length() == 1) {
            log.error("Exception", new ConditionsNotMetException("Электронная почта не может быть пустой и должна содержать символ @"));
            throw new ConditionsNotMetException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().contains(" ") || user.getLogin().isBlank()) {
            log.error("Exception", new ConditionsNotMetException("Логин не может быть пустым и содержать пробелы"));
            throw new ConditionsNotMetException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());
        if (user.getBirthday() != null) {
            if (user.getBirthday().isAfter(LocalDate.now())) {
                log.error("Exception", new ConditionsNotMetException("Дата рождения не может быть в будущем"));
                throw new ConditionsNotMetException("Дата рождения не может быть в будущем");
            }
        } else {
            log.error("Exception", new ConditionsNotMetException("Дата рождения не может быть нулевой"));
            throw new ConditionsNotMetException("Дата рождения не может быть нулевой");
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

    private void duplicateCheck(User user) throws DuplicatedDataException {
        for (User u : users.values())
            if (u.getEmail().equals(user.getEmail())) {
                log.error("Exception", new DuplicatedDataException("Этот имейл уже используется"));
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
    }

    public User update(@Valid User newUser) throws ConditionsNotMetException, NotFoundException, DuplicatedDataException {
        if (newUser.getId() == null) {
            log.error("Exception", new ConditionsNotMetException("Id должен быть указан"));
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getEmail() == null || newUser.getEmail().isBlank() || !newUser.getEmail().contains("@") || newUser.getEmail().contains(" ") || newUser.getEmail().length() == 1) {
                log.error("Exception", new ConditionsNotMetException("Электронная почта не может быть пустой и должна содержать символ @"));
                throw new ConditionsNotMetException("Электронная почта не может быть пустой и должна содержать символ @");
            } else if (!newUser.getEmail().equals(oldUser.getEmail())) {
                for (User u : users.values())
                    if (u.getEmail().equals(newUser.getEmail())) {
                        log.error("Exception", new DuplicatedDataException("Этот имейл уже используется"));
                        throw new DuplicatedDataException("Этот имейл уже используется");
                    }
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getLogin() == null || newUser.getLogin().contains(" ") || newUser.getLogin().isBlank()) {
                log.error("Exception", new ConditionsNotMetException("Логин не может быть пустым и содержать пробелы"));
                throw new ConditionsNotMetException("Логин не может быть пустым и содержать пробелы");
            } else oldUser.setLogin(newUser.getLogin());
            if (newUser.getName() == null || newUser.getName().isBlank()) oldUser.setName(newUser.getLogin());
            else oldUser.setName(newUser.getName());
            if (newUser.getBirthday() != null) {
                if (newUser.getBirthday().isAfter(LocalDate.now())) {
                    log.error("Exception", new ConditionsNotMetException("Дата рождения не может быть в будущем"));
                    throw new ConditionsNotMetException("Дата рождения не может быть в будущем");
                } else oldUser.setBirthday(newUser.getBirthday());
            } else {
                log.error("Exception", new ConditionsNotMetException("Дата рождения не может быть нулевой"));
                throw new ConditionsNotMetException("Дата рождения не может быть нулевой");
            }
            return oldUser;
        } else {
            log.error("Exception", new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден"));
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }
    }
}