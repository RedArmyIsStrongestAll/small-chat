package ru.coffee.smallchat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Schema(description = "объект получния сообщения из личного чата")
public class PersonalMessageResponseDTO {
    @Schema(description = "сообщение")
    private String message;
    @Schema(description = "время отправки")
    private String sendTime;
    @Schema(description = "uuid отправителя")
    private String producerUserUuid;
    @Schema(description = "uuid получателя")
    private String consumerUserUuid;
    @Schema(description = "id чата")
    private Long chatId;
    @Schema(description = "флаг кому принадлежит сообщение")
    private Boolean senderIsProducer;
    @Schema(description = "флаг является ли пользовтаель отправителем")
    private Boolean itIsProducer;
}
