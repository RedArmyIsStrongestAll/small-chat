package ru.coffee.smallchat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.coffee.smallchat.dto.ResponseDTO;
import ru.coffee.smallchat.entity.OAuthRegistry;
import ru.coffee.smallchat.service.LoginService;

@RestController()
@RequestMapping("/login")
public class LoginHttpController {
    private LoginService loginService;

    @Autowired
    public LoginHttpController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/vk")
    //todo operation swagger
    public ResponseDTO<Long> registryVk(@RequestBody() OAuthRegistry codeType) {
        return loginService.login(1);
    }
}
