package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class FriendRequest {
    @NotNull
    private Long user_id;
    private Long friend_id;
    private boolean accept = false;
}