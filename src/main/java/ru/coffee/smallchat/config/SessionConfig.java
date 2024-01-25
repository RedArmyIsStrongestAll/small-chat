package ru.coffee.smallchat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.time.Duration;
import java.util.Objects;

@Configuration
public class SessionConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setUseBase64Encoding(false);
        return serializer;
    }

    @Bean
    public SessionRepositoryCustomizer<JdbcIndexedSessionRepository> getSessionRepositoryCustomizer(@Autowired Environment env) {
        return sessionRepository -> {
            sessionRepository.setDefaultMaxInactiveInterval(Duration.ofMinutes
                    (Integer.parseInt(Objects.requireNonNull(env.getProperty("user.live.time.minutes")))));
            sessionRepository.setTableName("USER_SESSIONS");
        };
    }

}
