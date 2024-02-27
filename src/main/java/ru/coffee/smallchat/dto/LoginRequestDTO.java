package ru.coffee.smallchat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class LoginRequestDTO {
    @Schema(description = "тип аутентификации (id oauth сервиса)")
    private Integer type;
    @Schema(description = "широта gps")
    private Double latitude;
    @Schema(description = "долгота gps")
    private Double longitude;
}
