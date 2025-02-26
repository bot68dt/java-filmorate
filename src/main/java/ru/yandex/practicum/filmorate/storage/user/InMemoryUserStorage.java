package ru.yandex.practicum.filmorate.storage.user;

import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@NoArgsConstructor
@Slf4j(topic = "TRACE")
@ConfigurationPropertiesScan
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Collection<User> findAll() {
        log.info("Обработка Get-запроса...");
        return this.users.values();
    }

    public User findById(Long id) throws ConditionsNotMetException {
        log.info("Обработка Get-запроса...");
        if (id != 0 || !id.equals(null)) {
            Iterator var2 = this.users.values().iterator();

            User u;
            do {
                if (!var2.hasNext()) {
                    log.error("Exception", new NotFoundException(id.toString(), "Пользователь с данным идентификатором отсутствует в базе"));
                    throw new NotFoundException(id.toString(), "Пользователь с данным идентификатором отсутствует в базе");
                }

                u = (User) var2.next();
            } while (!u.getId().equals(Long.valueOf(id)));
            return u;
        } else {
            log.error("Exception", new ConditionsNotMetException(id.toString(), "Идентификатор пользователя не может быть нулевой"));
            throw new ConditionsNotMetException(id.toString(), "Идентификатор пользователя не может быть нулевой");
        }
    }

    public User create(@Valid User user) throws ConditionsNotMetException, DuplicatedDataException {
        log.info("Обработка Create-запроса...");
        this.duplicateCheck(user);
        if (user.getEmail() != null && !user.getEmail().isBlank() && user.getEmail().contains("@") && !user.getEmail().contains(" ") && user.getEmail().length() != 1) {
            if (user.getLogin() != null && !user.getLogin().contains(" ") && !user.getLogin().isBlank()) {
                if (user.getName() == null || user.getName().isBlank()) {
                    user.setName(user.getLogin());
                }

                if (user.getBirthday() != null) {
                    if (user.getBirthday().isAfter(LocalDate.now())) {
                        log.error("Exception", new ConditionsNotMetException(user.getBirthday().format(this.formatter), "Дата рождения не может быть в будущем"));
                        throw new ConditionsNotMetException(user.getBirthday().format(this.formatter), "Дата рождения не может быть в будущем");
                    } else {
                        user.setId(this.getNextId());
                        user.setFriends(new HashSet<>());
                        this.users.put(user.getId(), user);
                        return user;
                    }
                } else {
                    log.error("Exception", new ConditionsNotMetException(user.getBirthday().toString(), "Дата рождения не может быть нулевой"));
                    throw new ConditionsNotMetException(user.getBirthday().toString(), "Дата рождения не может быть нулевой");
                }
            } else {
                log.error("Exception", new ConditionsNotMetException(user.getLogin(), "Логин не может быть пустым и содержать пробелы"));
                throw new ConditionsNotMetException(user.getLogin(), "Логин не может быть пустым и содержать пробелы");
            }
        } else {
            log.error("Exception", new ConditionsNotMetException(user.getEmail(), "Электронная почта не может быть пустой и должна содержать символ @"));
            throw new ConditionsNotMetException(user.getEmail(), "Электронная почта не может быть пустой и должна содержать символ @");
        }
    }

    private long getNextId() {
        long currentMaxId = this.users.keySet().stream().mapToLong((id) -> {
            return id;
        }).max().orElse(0L);
        return ++currentMaxId;
    }

    private void duplicateCheck(User user) throws DuplicatedDataException {
        Iterator var2 = this.users.values().iterator();

        User u;
        do {
            if (!var2.hasNext()) {
                return;
            }

            u = (User) var2.next();
        } while (!u.getEmail().equals(user.getEmail()));
        log.error("Exception", new DuplicatedDataException(user.getEmail(), "Этот имейл уже используется"));
        throw new DuplicatedDataException(user.getEmail(), "Этот имейл уже используется");
    }

    public User update(@Valid User newUser) throws ConditionsNotMetException, NotFoundException, DuplicatedDataException {
        if (newUser.getId() == null) {
            log.error("Exception", new ConditionsNotMetException(newUser.getId().toString(), "Id должен быть указан"));
            throw new ConditionsNotMetException(newUser.getId().toString(), "Id должен быть указан");
        } else if (!this.users.containsKey(newUser.getId())) {
            log.error("Exception", new NotFoundException(newUser.getId().toString(), "Пользователь с указанным id не найден"));
            throw new NotFoundException(newUser.getId().toString(), "Пользователь с указанным id не найден");
        } else {
            User oldUser = (User) this.users.get(newUser.getId());
            if (newUser.getEmail() != null && !newUser.getEmail().isBlank() && newUser.getEmail().contains("@") && !newUser.getEmail().contains(" ") && newUser.getEmail().length() != 1) {
                if (!newUser.getEmail().equals(oldUser.getEmail())) {
                    this.duplicateCheck(newUser);
                    oldUser.setEmail(newUser.getEmail());
                }

                if (newUser.getLogin() != null && !newUser.getLogin().contains(" ") && !newUser.getLogin().isBlank()) {
                    oldUser.setLogin(newUser.getLogin());
                    if (newUser.getName() != null && !newUser.getName().isBlank()) {
                        oldUser.setName(newUser.getName());
                    } else {
                        oldUser.setName(newUser.getLogin());
                    }

                    if (newUser.getBirthday() != null) {
                        if (newUser.getBirthday().isAfter(LocalDate.now())) {
                            log.error("Exception", new ConditionsNotMetException(newUser.getBirthday().format(this.formatter), "Дата рождения не может быть в будущем"));
                            throw new ConditionsNotMetException(newUser.getBirthday().format(this.formatter), "Дата рождения не может быть в будущем");
                        } else {
                            oldUser.setBirthday(newUser.getBirthday());
                            return oldUser;
                        }
                    } else {
                        log.error("Exception", new ConditionsNotMetException(newUser.getBirthday().toString(), "Дата рождения не может быть нулевой"));
                        throw new ConditionsNotMetException(newUser.getBirthday().toString(), "Дата рождения не может быть нулевой");
                    }
                } else {
                    log.error("Exception", new ConditionsNotMetException(newUser.getLogin(), "Логин не может быть пустым и содержать пробелы"));
                    throw new ConditionsNotMetException(newUser.getLogin(), "Логин не может быть пустым и содержать пробелы");
                }
            } else {
                log.error("Exception", new ConditionsNotMetException(newUser.getEmail(), "Электронная почта не может быть пустой и должна содержать символ @"));
                throw new ConditionsNotMetException(newUser.getEmail(), "Электронная почта не может быть пустой и должна содержать символ @");
            }
        }
    }
}