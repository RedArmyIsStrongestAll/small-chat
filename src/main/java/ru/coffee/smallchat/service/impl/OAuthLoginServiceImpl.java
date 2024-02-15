package ru.coffee.smallchat.service.impl;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.coffee.smallchat.dto.OAuthRegistryResponseVkSilentTokenDTO;
import ru.coffee.smallchat.dto.ResponseDTO;
import ru.coffee.smallchat.dto.UserAuthDTO;
import ru.coffee.smallchat.entity.AbstractRegistry;
import ru.coffee.smallchat.entity.OAuthRegistry;
import ru.coffee.smallchat.entity.UserIdAuthenticationToken;
import ru.coffee.smallchat.repository.impl.PostgresMainRepositoryImpl;
import ru.coffee.smallchat.service.JwtService;
import ru.coffee.smallchat.service.LoginService;

import java.util.List;

@Service
@Slf4j
public class OAuthLoginServiceImpl implements LoginService {

    private final MainServiceImpl mainService;
    private final JwtService jwtService;
    private final Environment env;
    private final PostgresMainRepositoryImpl mainRepository;
    private final PrometheusMeterRegistry meterRegistry;


    public OAuthLoginServiceImpl(@Autowired MainServiceImpl mainService,
                                 @Autowired JwtService jwtService,
                                 @Autowired Environment env,
                                 @Autowired PostgresMainRepositoryImpl mainRepository,
                                 @Autowired PrometheusMeterRegistry meterRegistry) {
        this.mainService = mainService;
        this.jwtService = jwtService;
        this.env = env;
        this.mainRepository = mainRepository;
        this.meterRegistry = meterRegistry;
    }

    public String forward(Integer type) {
        switch (type) {
            case 1:
                return forwardVk();
            default:
                log.warn("OAuthLoginServiceImpl.forward - неожиданное поведение," +
                        "пришёл не существуюший код OAuth сервиса");
                return null;
        }
    }

    @Override
    public ResponseDTO<Long> login(AbstractRegistry type) {
        OAuthRegistry registry = (OAuthRegistry) type;

        String userId = null;
        switch (registry.getCodeType()) {
            case 1:
                loginVk(registry);
                break;
            default:
                return new ResponseDTO<>(500, "Данный OAuth сервиса не поддерживается");
        }
        if (registry.getId() == null) {
            return new ResponseDTO<>(500, "Не получилось осуществить вход в систему");
        }
        userId = registry.getId().toString();

        List<UserAuthDTO> userAuthDTOList = mainRepository.getUserByAuthId(userId);
        if (userAuthDTOList.isEmpty()) {
            mainRepository.rigestryUser(registry);
        } else {
            if (userAuthDTOList.size() != 1) {
                log.error("OAuthLoginServiceImpl.loginVk - неожиданное поведение," +
                        "существует несколько разных пользовтаелей под один authId");
                meterRegistry.counter("error_in_controller",
                        "method", "loginVk",
                        "id", userAuthDTOList.get(0).getUserId()).increment();
                return new ResponseDTO<>(500, "Не получилось осуществить вход в систему");
            }
            UserAuthDTO userAuthDTO = userAuthDTOList.get(0);
            mainRepository.reDeleteUser(userAuthDTO.getUserId());
        }

        mainService.addPUserToQueueForDelete(userId);

        Authentication authentication = new UserIdAuthenticationToken(userId);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwtToken = jwtService.createToken(userId);
        //todo комментарий в свагер, установить как Bearer токен
        registry.getResponse().addHeader(JwtService.HEADER_STRING, jwtToken);
        return new ResponseDTO<>(302, env.getProperty("oauth.rigestry.riderect"));
    }

    private String forwardVk() {
        return env.getProperty("vk.id.address.silent-token") +
                "?uuid=" +
                env.getProperty("vk.id.oauth.uuid") +
                "&app_id=" +
                env.getProperty("vk.id.oauth.app-id") +
                "&response_type=silent_token" +
                "&redirect_uri=" +
                env.getProperty("vk.id.oauth.riderect-uri");
    }

    private void loginVk(OAuthRegistry registry) {
        OAuthRegistryResponseVkSilentTokenDTO response = (OAuthRegistryResponseVkSilentTokenDTO) registry.getOAuthResponse();
        Long userId = response.getUser().getId();
        registry.setId(userId);
    }
}
