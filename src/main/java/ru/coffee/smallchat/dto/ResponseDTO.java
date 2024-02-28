package ru.coffee.smallchat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "объект оболочка ответа <br/>" +
        "вернётся error или response")
public class ResponseDTO<T> {
    public ResponseDTO(Integer code, String error) {
        this.code = code;
        this.error = error;
    }

    public ResponseDTO(Integer code, T response) {
        this.code = code;
        this.response = response;
    }

    @Schema(description = "код http ответа")
    private int code;
    @Schema(description = "текст ошибки")
    private String error;
    @Schema(description = "объект ответа")
    private T response;
}
