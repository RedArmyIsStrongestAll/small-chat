package ru.coffee.smallchat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Schema(description = "объект отправки сообщения в личный чат <br/>" +
        "ожидается consumerUserUuid или chatId")
public class PersonalMessageRequestDTO {
    @Schema(description = "сообщение")
    private String message;
    @Schema(description = "uuid получателя")
    private String consumerUserUuid;
    @Schema(description = "id чата")
    private Long chatId;
}
