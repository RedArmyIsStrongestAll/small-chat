package ru.coffee.smallchat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatAdapterWithFlagProducerDTO {
    private ChatDTO chatDTO;
    private Boolean userIsProducerInChat;
}
