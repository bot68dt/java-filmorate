package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.filmorate.exception.ImageFileException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Image;
import ru.yandex.practicum.filmorate.model.ImageData;
import ru.yandex.practicum.filmorate.service.ImageService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @GetMapping("/films/{filmId}/images")
    public List<Image> getPostImages(@PathVariable("filmId") long filmId) {
        return imageService.getPostImages(filmId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/films/{filmId}/images")
    public List<Image> addPostImages(@PathVariable("filmId") long filmId, @RequestParam("image") List<MultipartFile> files) {
        return imageService.saveImages(filmId, files);
    }

    @GetMapping(value = "/images/{imageId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadImage(@PathVariable long imageId) throws NotFoundException, ImageFileException {
        ImageData imageData = imageService.getImageData(imageId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(imageData.getName()).build());

        return new ResponseEntity<>(imageData.getData(), headers, HttpStatus.OK);
    }
}