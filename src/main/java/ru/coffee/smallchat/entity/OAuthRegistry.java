package ru.coffee.smallchat.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class OAuthRegistry implements AbstractRegistry {
    private Integer codeType;
}
