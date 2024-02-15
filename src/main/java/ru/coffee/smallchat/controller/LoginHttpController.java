package ru.coffee.smallchat.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.coffee.smallchat.dto.ResponseDTO;
import ru.coffee.smallchat.entity.OAuthRegistry;
import ru.coffee.smallchat.service.impl.OAuthLoginServiceImpl;

import java.net.URI;

/**
 * codeType:
 * vk - 1
 * ok - 2
 * mail ru - 3
 */
@RestController()
@RequestMapping("/login")
public class LoginHttpController {
    private OAuthLoginServiceImpl loginService;

    @Autowired
    public LoginHttpController(OAuthLoginServiceImpl loginService) {
        this.loginService = loginService;
    }
    //todo operation swagger

    @GetMapping("/oauth")
    public ResponseEntity<Void> sendToVk(@RequestParam(value = "codeType") Integer codeType) {
        String location = loginService.forward(codeType);
        return ResponseEntity.status(302).location(URI.create(location)).build();
    }

    @PostMapping("/vk")
    public ResponseDTO<Long> registryVk(HttpServletResponse response) {
        OAuthRegistry registry = new OAuthRegistry(1, response, null);
        return loginService.login(registry);
    }
}
