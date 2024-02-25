package ru.coffee.smallchat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.coffee.smallchat.dto.LoginDTO;
import ru.coffee.smallchat.dto.ResponseDTO;
import ru.coffee.smallchat.entity.OAuthRegistry;
import ru.coffee.smallchat.service.LoginService;

import java.net.URI;

/**
 * codeType:
 * vk, ok, mail_ru (VK ID) - 1
 */
@RestController()
@RequestMapping("/login")
public class LoginHttpController {
    private final LoginService loginService;

    public LoginHttpController(@Autowired LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/oauth")
    @Operation(summary = "переотправка на OAuth сервисы",
            description = "codeType: <br/>" +
                    "1 - VK ID")
    public ResponseEntity<String> relocation(@RequestParam(value = "codeType") Integer codeType) {
        String location = loginService.relocation(codeType);
        if (location != null) {
            return ResponseEntity.status(302).location(URI.create(location)).build();
        } else {
            return ResponseEntity.status(400).body("Ошибка переадрессации");
        }
    }

    @GetMapping("/vk")
    @Operation(summary = "пряинтие VK ID токена",
            description = "на этот метод переправляется сам VK ID, а сам метод переотпраивт пользовтаеля на главную страницу <br/>" +
                    "полученный jwtToken необходимо передвать в заголовке Authorization для каждого запроса")
    public ResponseDTO<LoginDTO> registryVk(@RequestParam("payload") String token) throws JsonProcessingException {
        OAuthRegistry registry = new OAuthRegistry(1, token, null);
        return loginService.login(registry);
    }
}
