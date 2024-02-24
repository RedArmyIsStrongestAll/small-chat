package ru.coffee.smallchat.service;

import ru.coffee.smallchat.dto.LoginDTO;
import ru.coffee.smallchat.dto.ResponseDTO;
import ru.coffee.smallchat.entity.AbstractRegistry;

public interface LoginService {
    String relocation(Integer type);

    ResponseDTO<LoginDTO> login(AbstractRegistry registry);
}
