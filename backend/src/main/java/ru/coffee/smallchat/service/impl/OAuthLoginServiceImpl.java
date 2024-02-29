package ru.coffee.smallchat.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.coffee.smallchat.dto.*;
import ru.coffee.smallchat.entity.GeolocationProperties;
import ru.coffee.smallchat.entity.UserIdAuthenticationToken;
import ru.coffee.smallchat.repository.MainRepository;
import ru.coffee.smallchat.service.JwtService;
import ru.coffee.smallchat.service.LoginService;
import ru.coffee.smallchat.service.MainService;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OAuthLoginServiceImpl implements LoginService {

    private final MainService mainService;
    private final JwtService jwtService;
    private final Environment env;
    private final MainRepository mainRepository;
    private final PrometheusMeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;
    private final GeolocationProperties geolocationProperties;

    public OAuthLoginServiceImpl(@Autowired MainService mainService,
                                 @Autowired JwtService jwtService,
                                 @Autowired Environment env,
                                 @Autowired MainRepository mainRepository,
                                 @Autowired PrometheusMeterRegistry meterRegistry,
                                 @Autowired GeolocationProperties geolocationProperties) {
        this.mainService = mainService;
        this.jwtService = jwtService;
        this.env = env;
        this.mainRepository = mainRepository;
        this.meterRegistry = meterRegistry;
        this.objectMapper = new ObjectMapper();
        this.geolocationProperties = geolocationProperties;
    }

    @Override
    public ResponseEntity<String> relocation(LoginRequestDTO loginRequestDTO) {
        if (!checkGeolocation(loginRequestDTO)) {
            return ResponseEntity.status(400).body("Геолокация не соответствует обслуживаемым заведениям");
        }

        String location = null;
        switch (loginRequestDTO.getType()) {
            case 1:
                location = relocationVk();
                break;
        }

        if (location != null) {
            return ResponseEntity.status(302).location(URI.create(location)).build();
        } else {
            log.warn("OAuthLoginServiceImpl.forward - неожиданное поведение," +
                    "пришёл не существуюший код OAuth сервиса");
            return ResponseEntity.status(400).body("Ошибка переадрессации");
        }
    }

    public Boolean checkGeolocation(LoginRequestDTO loginRequestDTO) {
        try {
            Map<String, GeolocationProperties.Coordinate> locations = geolocationProperties.getLocations();
            for (Map.Entry<String, GeolocationProperties.Coordinate> entry : locations.entrySet()) {
                GeolocationProperties.Coordinate coordinate = entry.getValue();
                if (isWithinRadius(loginRequestDTO.getLatitude(), loginRequestDTO.getLongitude(),
                        coordinate.getLatitude(), coordinate.getLongitude(),
                        Double.valueOf(env.getProperty("radius.location.meters")))) {
                    return true;
                }
            }
            return false;
        } catch (IllegalArgumentException e) {
            log.error("OAuthLoginServiceImpl.checkGeolocation - ошибка сравнения геолокации");
            meterRegistry.counter("error_in_controller",
                    "method", "relocation").increment();
            return false;
        }
    }

    private boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radius) {
        double earthRadius = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;
        return distance <= radius;
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
    public ResponseDTO<LoginResponseDTO> login(OAuthRegistryDTO registry) {
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
                        "method", "relocation",
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

        Authentication authentication = new UserIdAuthenticationToken(userId);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwtToken = jwtService.createToken(userId);
        return new ResponseDTO<>(200, new LoginResponseDTO(userId, jwtToken));
    }

    private String parseVk(String token) {
        OAuthRegistryResponseVkDTO registryVk;
        try {
            registryVk = objectMapper.readValue(token,
                    OAuthRegistryResponseVkDTO.class);
        } catch (JsonProcessingException e) {
            log.error("OAuthLoginServiceImpl.login - ошибка парсинга сообщения от VK ID");
            meterRegistry.counter("error_in_controller",
                    "method", "registryVk").increment();
            return null;
        }
        return registryVk.getUser().getId();
    }
}
