package ru.coffee.smallchat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserAuthDTO {
    private String userId;
    private String deletedAt;
}
