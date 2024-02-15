package ru.coffee.smallchat.entity;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OAuthRegistry implements AbstractRegistry {
    private Integer codeType;
    private HttpServletResponse response;
    private String token;
}
