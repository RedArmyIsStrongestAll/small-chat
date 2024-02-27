package ru.coffee.smallchat.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "coordinates")
@Data
public class GeolocationProperties {
    private Map<String, Coordinate> locations = new HashMap<>();

    @Data
    public static class Coordinate {
        private double latitude;
        private double longitude;
    }
}
