package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@EqualsAndHashCode(of = { "id" })
@AllArgsConstructor(staticName = "of")
public class Film {
    private Long id;
    @NotNull
    @NotBlank
    private String name;
    @Description("New film update description")
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    @NotNull
    private Integer duration;
    private Set<Long> likedUsers;
    @NotNull
    private Set<Long> genres;
    @NotNull
    private Long ratingId;

    public Map<String, Object> toMapFilm() {
        Map<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("name", name);
        values.put("description", description);
        values.put("releaseDate", releaseDate);
        values.put("duration", duration);
        values.put("ratingId", ratingId);
        return values;
    }
}