package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.util.List;

@Data
public class Genre {
    private static final List<String> genre = List.of("COMEDY", "DRAMA", "CARTOON", "THRILLER", "DOCUMENTARY", "ACTION");
}