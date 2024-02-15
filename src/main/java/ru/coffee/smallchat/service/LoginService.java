package ru.coffee.smallchat.service;

import ru.coffee.smallchat.dto.ResponseDTO;
import ru.coffee.smallchat.entity.AbstractRegistry;

public interface LoginService {
    ResponseDTO<Long> login(AbstractRegistry registry);
}
