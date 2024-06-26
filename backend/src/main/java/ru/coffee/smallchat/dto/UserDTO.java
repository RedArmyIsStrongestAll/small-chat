package ru.coffee.smallchat.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "объект собеседника по чату")
public class UserDTO {
    public UserDTO(String userId) {
        this.userId = userId;
    }

    @Schema(description = "id собеседника")
    private String userId;
    @Schema(description = "имя собеседника")
    private String name;
    @JsonIgnore
    private String photoPath;
    @Schema(description = "фотография собеседника")
    private byte[] photo;
    @Schema(description = "формат фотографии (пример: \"image/png\")")
    private String photoType;
}
