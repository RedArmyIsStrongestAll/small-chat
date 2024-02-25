package ru.coffee.smallchat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OAuthRegistry implements AbstractRegistry {
    private Integer codeType;
    private String oAuthResponse;
    private String id;
}
