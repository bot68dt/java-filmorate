/*package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ImageFileException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Image;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.ImageData;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final Map<Long, Image> images = new HashMap();
    private final String imageDirectory = "C:\\Users\\Dim\\Saves";
    private final FilmStorage filmStorage = new InMemoryFilmStorage();

    public List<Image> getPostImages(long filmtId) {
        return (List)this.images.values().stream().filter((image) -> {
            return image.getFilmId() == filmtId;
        }).collect(Collectors.toList());
    }

    private Path saveFile(MultipartFile file, Film film) {
        try {
            String uniqueFileName = String.format("%d.%s", Instant.now().toEpochMilli(), StringUtils.getFilenameExtension(file.getOriginalFilename()));
            Path uploadPath = Paths.get("C:\\Users\\Dim\\Saves", String.valueOf(film.getId().toString()));
            Path filePath = uploadPath.resolve(uniqueFileName);
            if (!Files.exists(uploadPath, new LinkOption[0])) {
                Files.createDirectories(uploadPath);
            }

            file.transferTo(filePath);
            return filePath;
        } catch (IOException var6) {
            IOException e = var6;
            throw new RuntimeException(e);
        }
    }

    public List<Image> saveImages(long filmId, List<MultipartFile> files) {
        return (List)files.stream().map((file) -> {
            try {
                return this.saveImage(filmId, file);
            } catch (ConditionsNotMetException var5) {
                ConditionsNotMetException e = var5;
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    private Image saveImage(Long filmId, MultipartFile file) throws ConditionsNotMetException {
        Film film = filmStorage.findById(filmId);
        Path filePath = this.saveFile(file, film);
        long imageId = this.getNextId();
        Image image = new Image();
        image.setId(imageId);
        image.setFilePath(filePath.toString());
        image.setFilmId(filmId);
        image.setOriginalFileName(file.getOriginalFilename());
        this.images.put(imageId, image);
        return image;
    }

    private long getNextId() {
        long currentMaxId = this.images.keySet().stream().mapToLong((id) -> {
            return id;
        }).max().orElse(0L);
        return ++currentMaxId;
    }

    public ImageData getImageData(long imageId) throws NotFoundException, ImageFileException {
        if (!this.images.containsKey(imageId)) {
            throw new NotFoundException(String.valueOf(imageId), "Изображение с указанным id не найдено");
        } else {
            Image image = (Image)this.images.get(imageId);
            byte[] data = this.loadFile(image);
            return new ImageData(data, image.getOriginalFileName());
        }
    }

    private byte[] loadFile(Image image) throws ImageFileException {
        Path path = Paths.get(image.getFilePath());
        String var10002;
        Long var10003;
        if (Files.exists(path, new LinkOption[0])) {
            try {
                return Files.readAllBytes(path);
            } catch (IOException var4) {
                var10002 = image.getId().toString();
                var10003 = image.getId();
                throw new ImageFileException(var10002, "Ошибка чтения файла.  Id: " + var10003 + ", name: " + image.getOriginalFileName());
            }
        } else {
            var10002 = image.getId().toString();
            var10003 = image.getId();
            throw new ImageFileException(var10002, "Файл не найден. Id: " + var10003 + ", name: " + image.getOriginalFileName());
        }
    }
}*/