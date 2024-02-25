package ru.coffee.smallchat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LoginDTO {
    @Schema(description = "id пользовтаеля")
    private String id;
    @Schema(description = "jwt token идентификации")
    private String jwtToken;
}
