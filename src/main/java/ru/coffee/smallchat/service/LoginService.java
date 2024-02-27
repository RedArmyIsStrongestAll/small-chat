package ru.coffee.smallchat.service;

import org.springframework.http.ResponseEntity;
import ru.coffee.smallchat.dto.LoginRequestDTO;
import ru.coffee.smallchat.dto.LoginResponseDTO;
import ru.coffee.smallchat.dto.OAuthRegistryDTO;
import ru.coffee.smallchat.dto.ResponseDTO;

public interface LoginService {
    ResponseEntity<String> relocation(LoginRequestDTO loginRequestDTO);

    ResponseDTO<LoginResponseDTO> login(OAuthRegistryDTO registry);
}
