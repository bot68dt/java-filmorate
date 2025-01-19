package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j(topic = "TRACE")
@ConfigurationPropertiesScan
public class UserService implements UserInterface {
    UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public User addFriend(String idUser, String idFriend) throws ConditionsNotMetException {
        log.info("Обработка Post-запроса...");
        if (userStorage.findById(idUser).getFriends() != null && userStorage.findById(idUser).getFriends().contains(userStorage.findById(idFriend))) {
            log.error("Exception", new ConditionsNotMetException(idFriend, "Пользователь с данным идентификатором уже добавлен в друзья"));
            throw new ConditionsNotMetException(idFriend, "Пользователь с данным идентификатором уже добавлен в друзья");
        } else {
            userStorage.findById(idUser).getFriends().add(userStorage.findById(idFriend));
            userStorage.findById(idFriend).getFriends().add(userStorage.findById(idUser));
            return userStorage.findById(idFriend);
        }
    }

    @Override
    public User delFriend(String idUser, String idFriend) throws ConditionsNotMetException {
        log.info("Обработка Del-запроса...");
        if (!userStorage.findById(idUser).getFriends().contains(userStorage.findById(idFriend))) {
            log.error("Exception", new ConditionsNotMetException(idFriend, "Пользователь с данным идентификатором не является другом"));
            throw new ConditionsNotMetException(idFriend, "Пользователь с данным идентификатором не является другом");
        } else {
            userStorage.findById(idUser).getFriends().remove(userStorage.findById(idFriend));
            userStorage.findById(idFriend).getFriends().remove(userStorage.findById(idUser));
            return userStorage.findById(idFriend);
        }
    }

    @Override
    public Set<User> findJointFriends(String idUser, String idFriend) throws NotFoundException {
        log.info("Обработка Get-запроса...");
        Set<User> result = new HashSet<>(userStorage.findById(idUser).getFriends());
        result.retainAll(userStorage.findById(idFriend).getFriends());
        /*if (result.isEmpty()) {
            log.error("Exception", new NotFoundException(idUser, "Общие друзья с пользователем ID = " + idFriend + "отсутствуют."));
            throw new NotFoundException(idUser, "Общие друзья с пользователем ID = " + idFriend + "отсутствуют.");
        }*/
        return result;
    }

    @Override
    public Set<User> findAllFriends(String idUser) throws NotFoundException {
        log.info("Обработка Get-запроса...");
        return userStorage.findById(idUser).getFriends();
    }
}