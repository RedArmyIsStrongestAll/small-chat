package ru.coffee.smallchat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.coffee.smallchat.dto.OAuthRegistryResponseVkSilentTokenDTO;
import ru.coffee.smallchat.dto.ResponseDTO;
import ru.coffee.smallchat.entity.OAuthRegistry;
import ru.coffee.smallchat.service.impl.OAuthLoginServiceImpl;

import java.net.URI;

/**
 * codeType:
 * vk, ok, mail_ru (VK ID) - 1
 */
@RestController()
@RequestMapping("/login")
public class LoginHttpController {
    private final OAuthLoginServiceImpl loginService;
    private final ObjectMapper objectMapper;

    @Autowired
    public LoginHttpController(OAuthLoginServiceImpl loginService) {
        this.loginService = loginService;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping("/oauth")
    public ResponseEntity<Void> relocation(@RequestParam(value = "codeType") Integer codeType) {
        String location = loginService.forward(codeType);
        return ResponseEntity.status(302).location(URI.create(location)).build();
    }

    @GetMapping("/vk")
    public ResponseEntity<String> registryVk(HttpServletResponse response,
                                             @RequestParam("payload") String token) throws JsonProcessingException {
        OAuthRegistryResponseVkSilentTokenDTO responseVkSilentTokenDTO;
        try {
            responseVkSilentTokenDTO = objectMapper.readValue(token, OAuthRegistryResponseVkSilentTokenDTO.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(500).body("Ошибка получения данных от VK ID");
        }
        OAuthRegistry registry = new OAuthRegistry(1, responseVkSilentTokenDTO, null, response);
        ResponseDTO<Long> responseDTO = loginService.login(registry);
        if (responseDTO.getCode().equals(302)) {
            return ResponseEntity.status(302).location(URI.create(responseDTO.getError())).build();
        } else {
            return ResponseEntity.status(responseDTO.getCode()).body(responseDTO.getError());
        }
    }
}
