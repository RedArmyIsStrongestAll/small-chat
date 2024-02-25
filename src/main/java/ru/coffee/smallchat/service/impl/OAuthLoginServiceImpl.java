package ru.coffee.smallchat.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.coffee.smallchat.dto.LoginDTO;
import ru.coffee.smallchat.dto.OAuthRegistryResponseVkDTO;
import ru.coffee.smallchat.dto.ResponseDTO;
import ru.coffee.smallchat.dto.UserAuthDTO;
import ru.coffee.smallchat.entity.AbstractRegistry;
import ru.coffee.smallchat.entity.OAuthRegistry;
import ru.coffee.smallchat.entity.UserIdAuthenticationToken;
import ru.coffee.smallchat.repository.MainRepository;
import ru.coffee.smallchat.service.JwtService;
import ru.coffee.smallchat.service.LoginService;
import ru.coffee.smallchat.service.MainService;

import java.util.List;

@Service
@Slf4j
public class OAuthLoginServiceImpl implements LoginService {

    private final MainService mainService;
    private final JwtService jwtService;
    private final Environment env;
    private final MainRepository mainRepository;
    private final PrometheusMeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;


    public OAuthLoginServiceImpl(@Autowired MainService mainService,
                                 @Autowired JwtService jwtService,
                                 @Autowired Environment env,
                                 @Autowired MainRepository mainRepository,
                                 @Autowired PrometheusMeterRegistry meterRegistry) {
        this.mainService = mainService;
        this.jwtService = jwtService;
        this.env = env;
        this.mainRepository = mainRepository;
        this.meterRegistry = meterRegistry;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String relocation(Integer type) {
        switch (type) {
            case 1:
                return relocationVk();
            default:
                log.warn("OAuthLoginServiceImpl.forward - неожиданное поведение," +
                        "пришёл не существуюший код OAuth сервиса");
                return null;
        }
    }

    private String relocationVk() {
        return env.getProperty("vk.id.address.silent-token") +
                "?uuid=" +
                env.getProperty("vk.id.oauth.uuid") +
                "&app_id=" +
                env.getProperty("vk.id.oauth.app-id") +
                "&response_type=silent_token" +
                "&redirect_uri=" +
                env.getProperty("vk.id.oauth.riderect-uri");
    }

    @Override
    public ResponseDTO<LoginDTO> login(AbstractRegistry type) {
        OAuthRegistry registry = (OAuthRegistry) type;

        String oauthUserId;
        switch (registry.getCodeType()) {
            case 1:
                registry.setId(parseVk(registry.getOAuthResponse()));
                break;
            default:
                return new ResponseDTO<>(500, "Данный OAuth сервис не поддерживается");
        }
        if (registry.getId() == null) {
            return new ResponseDTO<>(500, "Не получилось осуществить вход в систему");
        }
        oauthUserId = registry.getId();

        List<UserAuthDTO> userAuthDTOList = mainRepository.getUserByAuthId(oauthUserId);
        String userId;
        if (userAuthDTOList.isEmpty()) {
            userId = mainRepository.rigestryUser(registry);
            mainService.addPUserToQueueForDelete(userId);
        } else {
            if (userAuthDTOList.size() != 1) {
                log.error("OAuthLoginServiceImpl.login - неожиданное поведение," +
                        "существует несколько разных пользовтаелей под один authId");
                meterRegistry.counter("error_in_controller",
                        "method", "login",
                        "id", userAuthDTOList.get(0).getUserId()).increment();
                return new ResponseDTO<>(500, "Не получилось осуществить вход в систему");
            }
            UserAuthDTO userAuthDTO = userAuthDTOList.get(0);
            userId = userAuthDTO.getUserId();
            if (userAuthDTO.getDeletedAt() != null) {
                mainRepository.reDeleteUser(userId);
                mainService.addPUserToQueueForDelete(userId);
            }
            mainRepository.updateLastLoginTime(userId);
        }

        Authentication authentication = new UserIdAuthenticationToken(oauthUserId);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwtToken = jwtService.createToken(userId);
        return new ResponseDTO<>(200, new LoginDTO(userId, jwtToken));
    }

    private String parseVk(String token) {
        OAuthRegistryResponseVkDTO registryVk;
        try {
            registryVk = objectMapper.readValue(token,
                    OAuthRegistryResponseVkDTO.class);
        } catch (JsonProcessingException e) {
            log.error("OAuthLoginServiceImpl.login - ошибка парсинга сообщения от VK ID");
            meterRegistry.counter("error_in_controller",
                    "method", "login").increment();
            return null;
        }
        return registryVk.getUser().getId();
    }
}
