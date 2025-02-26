package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j(topic = "TRACE")
@ConfigurationPropertiesScan
@RequiredArgsConstructor
public class UserService implements UserInterface {
    @Autowired
    @Qualifier("UserDbStorage")
    UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;


    @Override
    public User addFriend(Long idUser, Long idFriend) throws ConditionsNotMetException {
        log.info("Обработка Post-запроса...");
        String sqlQuery2 = "select userId, friendId from friends";
        Map<Long, Set<Long>> friends = jdbcTemplate.query(sqlQuery2, new UserDbStorage.FriendsExtractor());

        if (friends.get(idUser) != null && friends.get(idUser).contains(idFriend)) {
            log.error("Exception", new ConditionsNotMetException(idFriend.toString(), "Пользователь с данным идентификатором уже добавлен в друзья"));
            throw new ConditionsNotMetException(idFriend.toString(), "Пользователь с данным идентификатором уже добавлен в друзья");
        }
        if (userStorage.findById(idFriend) == null) {
            log.error("Exception", new NotFoundException(idFriend.toString(), "Пользователь с данным идентификатором отсутствует в базе"));
            throw new NotFoundException(idFriend.toString(), "Пользователь с данным идентификатором отсутствует в базе");
        }
        String sqlQuery = "insert into friends(userId, friendId) " + "values (?, ?)";
        jdbcTemplate.update(sqlQuery, idUser, idFriend);
        jdbcTemplate.update(sqlQuery, idFriend, idUser);
        return userStorage.findById(idUser);

    }

    @Override
    public User delFriend(Long idUser, Long idFriend) throws ConditionsNotMetException {
        log.info("Обработка Del-запроса...");
        String sqlQuery = "delete from friends where userId = ? and friendId = ?";
        jdbcTemplate.update(sqlQuery, idUser, idFriend);
        if (userStorage.findById(idUser) == null) {
            log.error("Exception", new NotFoundException(idUser.toString(), "Пользователь с данным идентификатором отсутствует в базе"));
            throw new NotFoundException(idUser.toString(), "Пользователь с данным идентификатором отсутствует в базе");
        }
        if (userStorage.findById(idFriend) == null) {
            log.error("Exception", new NotFoundException(idUser.toString(), "Пользователь с данным идентификатором отсутствует в базе"));
            throw new NotFoundException(idUser.toString(), "Пользователь с данным идентификатором отсутствует в базе");
        }
        return userStorage.findById(idUser);
    }

    @Override
    public Set<Long> findJointFriends(Long idUser, Long idFriend) throws NotFoundException {
        log.info("Обработка Get-запроса...");
        String sqlQuery2 = "select userId, friendId from friends";
        Map<Long, Set<Long>> friends = jdbcTemplate.query(sqlQuery2, new UserDbStorage.FriendsExtractor());
        Set<Long> result = new HashSet<>(friends.get(idUser));
        result.retainAll(friends.get(idFriend));
        return result;
    }

    @Override
    public Set<Long> findAllFriends(Long idUser) throws NotFoundException {
        log.info("Обработка Get-запроса...");
        String sqlQuery2 = "select userId, friendId from friends";
        Map<Long, Set<Long>> friends = jdbcTemplate.query(sqlQuery2, new UserDbStorage.FriendsExtractor());
        if (userStorage.findById(idUser) == null) {
            log.error("Exception", new NotFoundException(idUser.toString(), "Пользователь с данным идентификатором отсутствует в базе"));
            throw new NotFoundException(idUser.toString(), "Пользователь с данным идентификатором отсутствует в базе");
        }
        return friends.get(idUser);
    }
}