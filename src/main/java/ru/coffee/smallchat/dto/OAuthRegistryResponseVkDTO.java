package ru.coffee.smallchat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OAuthRegistryResponseVkDTO {
    private String type;
    private Integer auth;
    private User user;
    private Object token;
    private Integer ttl;
    private String uuid;
    private String hash;
    private Boolean loadExternalUsers;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public class User {
        private String id;
        private String first_name;
        private String last_name;
        private String avatar;
        private String avatar_base;
        private String phone;
    }
}
