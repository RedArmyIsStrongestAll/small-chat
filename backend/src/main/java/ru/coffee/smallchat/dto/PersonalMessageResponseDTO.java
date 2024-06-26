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
    @Schema(description = "id отправителя")
    private String producerUserId;
    @Schema(description = "id получателя")
    private String consumerUserId;
    @Schema(description = "id чата")
    private long chatId;
    @Schema(description = "флаг кто отправил сообщение в рамках этого чата: отпарвителю или получателю " +
            "(для удобаства отображения сообщений)")
    private Boolean senderIsProducerInChat;
    @Schema(description = "флаг является ли пользовтаель отправителем в рамках этого чата " +
            "(для получения истории)")
    private Boolean userIsProducerInChat;
}
