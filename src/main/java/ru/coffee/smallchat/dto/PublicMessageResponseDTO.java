package ru.coffee.smallchat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Schema(description = "объект получения сообщения из общего чата")
public class PublicMessageResponseDTO {
    @Schema(description = "сообщение")
    private String message;
    @Schema(description = "время отправки сообщения")
    private String sendTime;
    @Schema(description = "uuid отправителя")
    private String producerUserUuid;
}
