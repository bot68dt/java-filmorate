package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.util.List;

@Data
public class Rating {
    private final static List<String> raiting = List.of("G", "PG", "PG-13", "R", "NC-17");
}