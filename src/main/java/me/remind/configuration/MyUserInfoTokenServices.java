package me.remind.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import me.remind.model.Role;
import me.remind.model.User;
import me.remind.repository.UserRepository;

/**
 * By vlad.oltean on 04/07/2018.
 */
public class MyUserInfoTokenServices extends UserInfoTokenServices {

    private UserRepository userRepository;

    public MyUserInfoTokenServices(String userInfoEndpointUrl, String clientId, UserRepository userRepository) {
        super(userInfoEndpointUrl, clientId);
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
        OAuth2Authentication oAuth2Authentication = super.loadAuthentication(accessToken);

        String principal = (String) oAuth2Authentication.getPrincipal();

        User user = userRepository.findOneByFbUserId(principal);

        if (user == null) {
            user = new User();
            user.setName((String)((HashMap) (oAuth2Authentication.getUserAuthentication().getDetails())).get("name"));
            user.setFbAccessToken(accessToken);
            user.setRoles(Role.getRoles(Collections.singletonList(Role.USER)));
            user.setFbUserId(principal);

            userRepository.save(user);
        }



        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                oAuth2Authentication.getPrincipal(),
                "N/A",
                Role.convert(user.getRoles()));

        token.setDetails(oAuth2Authentication.getDetails());




        return new OAuth2Authentication(oAuth2Authentication.getOAuth2Request(), token);
    }
}
