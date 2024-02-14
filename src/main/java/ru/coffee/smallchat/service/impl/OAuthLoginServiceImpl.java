package ru.coffee.smallchat.service.impl;

import org.springframework.stereotype.Service;
import ru.coffee.smallchat.dto.ResponseDTO;
import ru.coffee.smallchat.service.LoginService;

@Service
public class OAuthLoginServiceImpl implements LoginService {

    @Override
    public ResponseDTO<Long> login(Integer type) {

        switch (type) {
            case 1:
                loginVk();
                break;
            case 2:
                loginOk();
                break;
            case 3:
                loginMailRu();
                break;

            default:
        }
        return null;
    }

    private void loginVk() {

    }

    private void loginOk() {

    }

    private void loginMailRu() {

    }
}