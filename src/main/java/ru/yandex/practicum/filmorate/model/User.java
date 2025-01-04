package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = {"email"})
@AllArgsConstructor(staticName = "of")
public class User {
    private Long id;
    private String name;
    @Email
    private String email;
    @NonNull
    @NotBlank
    private String login;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
}