package net.causw.application.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OauthTokenDto {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private int expires_in;
    private String scope;
    private int refresh_token_expires_in;
}
