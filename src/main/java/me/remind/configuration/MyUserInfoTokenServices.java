package me.remind.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
    private String userInfoEndpointUrl;

    public MyUserInfoTokenServices(String userInfoEndpointUrl, String clientId, UserRepository userRepository) {
        super(userInfoEndpointUrl, clientId);
        this.userInfoEndpointUrl = userInfoEndpointUrl;
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
        OAuth2Authentication oAuth2Authentication = super.loadAuthentication(accessToken);

        String principal = (String) oAuth2Authentication.getPrincipal();
        String name = (String) ((HashMap) (oAuth2Authentication.getUserAuthentication().getDetails())).get("name");

        User user = handleUser(principal, accessToken, name);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                oAuth2Authentication.getPrincipal(),
                "N/A",
                Role.convertToAuthorities(user.getRoles()));

        token.setDetails(oAuth2Authentication.getUserAuthentication().getDetails());
        return new OAuth2Authentication(oAuth2Authentication.getOAuth2Request(), token);
    }

    /**
     * Save user to database if does not exist.
     * It also updates his name and access token for the specific login method used, if exists.
     *
     * @param id - from the principal, which may be wither of the oauth server ids.
     * @param accessToken - the access token specific to the oauth server used.
     * @param name - name of the user.
     * @return - saved Entity.
     */
    private User handleUser(String id, String accessToken, String name) {
        UserType userType = getUserType();
        User user;

        switch (userType) {
            case FACEBOOK:
                user = userRepository.findOneByFbUserId(id);
                break;
            case GOOGLE:
                user = userRepository.findOneByGoogleUserId(id);
                break;
            default:
                throw new RuntimeException("User type is not supported!");
        }

        // if access token or name have changed, update them

        if (user == null) { //CREATE USER
            user = new User();

            List<Role> roles = new ArrayList<>();
            roles.add(Role.USER);

            switch(userType){
                case FACEBOOK:
                    user.setFbUserId(id);
                    roles.add(Role.FACEBOOK);
                    break;
                case GOOGLE:
                    user.setGoogleUserId(id);
                    roles.add(Role.GOOGLE);
                    break;
                default:
                    throw new RuntimeException("User type is not supported!");
            }
            user.setRoles(Role.getRoles(roles));
        }

        //UPDATE USER:
        user.setName(name);

        int roles;
        switch(userType){
            case FACEBOOK:
                user.setFbAccessToken(accessToken);
                roles = Role.addRole(user.getRoles(), Role.FACEBOOK);
                break;
            case GOOGLE:
                user.setGoogleAccessToken(accessToken);
                roles = Role.addRole(user.getRoles(), Role.GOOGLE);
                break;
            default:
                throw new RuntimeException("User type is not supported!");
        }
        user.setRoles(roles);

        return userRepository.save(user);
    }



    private UserType getUserType() {
        if (this.userInfoEndpointUrl.contains("facebook")) {
            return UserType.FACEBOOK;
        }
        if (this.userInfoEndpointUrl.contains("google")) {
            return UserType.GOOGLE;
        }
        throw new RuntimeException("User Type could not be determined");
    }

    enum UserType {
        FACEBOOK,
        GOOGLE
    }

}
