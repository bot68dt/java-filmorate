package ru.yandex.practicum.filmorate.service;

import java.util.Set;

public interface UserInterface {
    public boolean addFriend(String idUser, String idFriend);

    public boolean delFriend(String idUser, String idFriend);

    public Set<Long> findJointFriends(String idUser, String idFriend);

    public Set<Long> findAllFriends(String idUser);
}