package ru.coffee.smallchat.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.coffee.smallchat.entity.UserIdAuthenticationToken;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    public static final String HEADER_NAME = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    private String secretKey;
    private Long timeToLiveToken;
    private final PrometheusMeterRegistry meterRegistry;

    public JwtService(@Autowired PrometheusMeterRegistry meterRegistry,
                      @Value("${jwt.live.time.minutes}") Long timeToLiveToken,
                      @Value("${jwt.token.secret.key}") String secretKey) {
        this.meterRegistry = meterRegistry;
        this.timeToLiveToken = timeToLiveToken * 1000 * 60;
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String userId) {
        Claims claims = Jwts.claims().setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + timeToLiveToken));
        return TOKEN_PREFIX + Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS256, secretKey).compact();
    }

    public String getFromHeader(HttpServletRequest req) {
        return req.getHeader(HEADER_NAME);
    }

    public boolean validation(String jwtToken) {
        Date dateEnd = getClaim(jwtToken, Claims::getExpiration);
        if (dateEnd == null) {
            return false;
        }
        return dateEnd.after(new Date());
    }

    public UserIdAuthenticationToken getAuthentication(String jwtToken) {
        String sub = getClaim(jwtToken, Claims::getSubject);
        if (sub == null || sub.isEmpty()) {
            log.error("JwtService.getAuthentication - неожиданное поведение, " +
                    "не получен id пользователя");
            meterRegistry.counter("error_in_service",
                    "method", "getAuthentication").increment();
            return null;
        }
        return new UserIdAuthenticationToken(sub);
    }

    private <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
        return claimsResolver.apply(claims);
    }
}
