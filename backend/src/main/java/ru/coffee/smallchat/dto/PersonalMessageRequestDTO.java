package ru.coffee.smallchat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "объект отправки сообщения в личный чат <br/>" +
        "ожидается consumerUserId или chatId")
public class PersonalMessageRequestDTO {
    @Schema(description = "сообщение")
    private String message;
    @Schema(description = "id получателя")
    private String consumerUserId;
    @Schema(description = "id чата")
    private Long chatId;
}
