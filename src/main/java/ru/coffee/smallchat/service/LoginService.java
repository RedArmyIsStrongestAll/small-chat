package ru.coffee.smallchat.service;

import ru.coffee.smallchat.dto.ResponseDTO;

public interface LoginService {
    ResponseDTO<Long> login(Integer type);
}
