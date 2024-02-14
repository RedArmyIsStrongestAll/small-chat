package ru.coffee.smallchat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class FileForDeleteDTO {
    private String userId;
    private LocalDateTime time;
}
