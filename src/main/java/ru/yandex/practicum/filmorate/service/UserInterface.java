package ru.yandex.practicum.filmorate.service;

import java.util.Set;

public interface UserInterface {
    public String addFriend(String idUser, String idFriend);

    public String delFriend(String idUser, String idFriend);

    public Set<Long> findJointFriends(String idUser, String idFriend);

    public Set<Long> findAllFriends(String idUser);
}