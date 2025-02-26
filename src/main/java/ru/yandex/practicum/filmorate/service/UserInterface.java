package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Set;

public interface UserInterface {
    public User addFriend(Long idUser, Long idFriend);

    public User delFriend(Long idUser, Long idFriend);

    public Set<User> findJointFriends(Long idUser, Long idFriend);

    public Set<User> findAllFriends(Long idUser);
}