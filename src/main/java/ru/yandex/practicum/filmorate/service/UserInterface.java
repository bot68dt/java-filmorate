package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Set;

public interface UserInterface {
    public boolean addFriend(String idUser, String idFriend);

    public boolean delFriend(String idUser, String idFriend);

    public Set<User> findJointFriends(String idUser, String idFriend);

    public Set<User> findAllFriends(String idUser);
}