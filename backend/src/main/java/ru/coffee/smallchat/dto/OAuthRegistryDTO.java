package ru.coffee.smallchat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OAuthRegistryDTO {
    private int codeType;
    private String oAuthResponse;
    private String id;
}
