package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Image {
    private Long id;
    private long filmId;
    private String originalFileName;
    private String filePath;
}