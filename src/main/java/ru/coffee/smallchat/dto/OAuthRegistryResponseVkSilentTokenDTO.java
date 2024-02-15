package ru.coffee.smallchat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.coffee.smallchat.entity.AbstractRigestryResponse;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OAuthRegistryResponseVkSilentTokenDTO implements AbstractRigestryResponse {
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
        private Long id;
        private String first_name;
        private String last_name;
        private String avatar;
        private String avatar_base;
        private String phone;
    }
}
