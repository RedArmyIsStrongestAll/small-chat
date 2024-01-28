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
    @Schema(description = "время отправки (пример: \"13:37\" )")
    private String sendTime;
    @Schema(description = "uuid отправителя")
    private String producerUserUuid;
    @Schema(description = "uuid получателя")
    private String consumerUserUuid;
    @Schema(description = "id чата")
    private Long chatId;
    @Schema(description = "флаг кому принадлежит сообщение в рамках этого чата: отпарвителю или получателю")
    private Boolean senderIsProducer;
    @Schema(description = "флаг является ли пользовтаель отправителем в рамках этого чата")
    private Boolean itIsProducer;
}
