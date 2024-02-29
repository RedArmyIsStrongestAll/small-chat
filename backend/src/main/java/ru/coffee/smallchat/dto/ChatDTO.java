package ru.coffee.smallchat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "объект чата")
public class ChatDTO {
    @Schema(description = "id чата")
    private long chatId;
    @Schema(description = "объект собеседника по чату")
    private UserDTO partnerUser;
}
