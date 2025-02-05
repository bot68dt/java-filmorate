package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.util.List;

@Data
public class Genre {
    private final static List<String> genre = List.of("COMEDY", "DRAMA", "CARTOON", "THRILLER", "DOCUMENTARY", "ACTION");
}