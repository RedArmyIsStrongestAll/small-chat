package ru.coffee.smallchat.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import ru.coffee.smallchat.dto.ResponseDTO;
import ru.coffee.smallchat.entity.AbstractRegistry;
import ru.coffee.smallchat.entity.OAuthRegistry;
import ru.coffee.smallchat.service.JwtService;
import ru.coffee.smallchat.service.LoginService;

@Service
public class OAuthLoginServiceImpl implements LoginService {

    private final MainServiceImpl mainService;
    private final JwtService jwtService;
    private final Environment env;

    public OAuthLoginServiceImpl(@Autowired MainServiceImpl mainService,
                                 @Autowired JwtService jwtService,
                                 @Autowired Environment env) {
        this.mainService = mainService;
        this.jwtService = jwtService;
        this.env = env;
    }

    public String forward(Integer type) {
        //todo в переменные внешние
        switch (type) {
            case 1:
                return "http:/localhost:1212";
            case 2:
                return "forward:/localhost:1213";
            case 3:
                return "forward:/localhost:1214";
            default:
                return null;
        }
    }

    @Override
    public ResponseDTO<Long> login(AbstractRegistry type) {
        OAuthRegistry registry = (OAuthRegistry) type;

        String jwtToken = null;
        switch (registry.getCodeType()) {
            case 1:
                jwtToken = loginVk();
            case 2:
                loginOk();
            case 3:
                loginOk();
        }

        //todo комментарий в свагер, установить как Bearer токен
        registry.getResponse().addHeader(JwtService.HEADER_STRING, jwtToken);
        mainService.addPUserToQueueForDelete(null);
        return new ResponseDTO<>(200, 123L);
    }

    private String loginVk() {
        //сохраняю в бд
        //сохраняю в контекст по id
        return jwtService.createToken("123");
    }

    private void loginOk() {

    }

    private void loginMailRu() {

    }
}
