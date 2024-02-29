package ru.coffee.smallchat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.coffee.smallchat.dto.LoginRequestDTO;
import ru.coffee.smallchat.dto.LoginResponseDTO;
import ru.coffee.smallchat.dto.OAuthRegistryDTO;
import ru.coffee.smallchat.dto.ResponseDTO;
import ru.coffee.smallchat.service.LoginService;

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

    @PostMapping("/oauth")
    @Operation(summary = "переотправка на OAuth сервисы",
            description = "codeType: <br/>" +
                    "1 - VK ID")
    public ResponseEntity<String> relocation(@RequestBody LoginRequestDTO loginRequestDTO) {
        return loginService.relocation(loginRequestDTO);
    }

    @GetMapping("/vk")
    @Operation(summary = "пряинтие VK ID токена",
            description = "на этот метод переправляется сам VK ID, а сам метод переотпраивт пользовтаеля на главную страницу <br/>" +
                    "полученный jwtToken необходимо передвать в заголовке Authorization для каждого запроса")
    public ResponseDTO<LoginResponseDTO> registryVk(@RequestParam("payload") String token) throws JsonProcessingException {
        OAuthRegistryDTO registry = new OAuthRegistryDTO(1, token, null);
        return loginService.login(registry);
    }
}
