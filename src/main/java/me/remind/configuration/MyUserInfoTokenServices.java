package me.remind.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.StringUtils;

import me.remind.model.Role;
import me.remind.model.User;
import me.remind.repository.UserRepository;

/**
 * By vlad.oltean on 04/07/2018.
 */
public class MyUserInfoTokenServices extends UserInfoTokenServices {

    private UserRepository userRepository;
    private String userInfoEndpointUrl;
    private String clientId;
    private String tokenType = DefaultOAuth2AccessToken.BEARER_TYPE;

    private OAuth2RestTemplate restTemplate;

    public MyUserInfoTokenServices(String userInfoEndpointUrl, String clientId, UserRepository userRepository) {
        super(userInfoEndpointUrl, clientId);
        this.clientId = clientId;
        this.userInfoEndpointUrl = userInfoEndpointUrl;
        this.userRepository = userRepository;


    }

    // TODO: Refactor this.
    private OAuth2RestTemplate getOAuth2RestTemplate(String accessToken) {
//        OAuth2RestOperations restTemplate = this.restTemplate;
        if (restTemplate == null) {
            BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
            resource.setClientId(this.clientId);
            restTemplate = new OAuth2RestTemplate(resource);
        }
        OAuth2AccessToken existingToken = restTemplate.getOAuth2ClientContext()
                .getAccessToken();
        if (existingToken == null || !accessToken.equals(existingToken.getValue())) {
            DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(
                    accessToken);
            token.setTokenType(this.tokenType);
            restTemplate.getOAuth2ClientContext().setAccessToken(token);
        }
        return this.restTemplate;
//        return restTemplate.getForEntity(path, Map.class).getBody();
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

        // TODO: refactor this
        Map<String, String> authDetails = (Map<String, String>) oAuth2Authentication.getUserAuthentication().getDetails();
        authDetails.put("email", user.getEmail());

        token.setDetails(authDetails);
        return new OAuth2Authentication(oAuth2Authentication.getOAuth2Request(), token);
    }

    /**
     * Save user to database if does not exist.
     * It also updates his name and access token for the specific login method used, if exists.
     *
     * @param id          - from the principal, which may be either of the oauth server ids.
     * @param accessToken - the access token specific to the oauth server used.
     * @param name        - name of the user.
     * @return - saved Entity.
     */
    private User handleUser(String id, String accessToken, String name) {
        UserType userType = getUserType();
        User user;

        String userEmail = getUserEmail(id, accessToken);

        // search by email
        // get users email in initial call and add it to the Principal object :)
        user = userRepository.findOneByEmail(userEmail);

        if (user == null) {
            //if no user was found by email, search by id.
            // Refuse users that do not have email?
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
        }


        //CREATE USER
        if (user == null) {
            user = new User();

            List<Role> roles = new ArrayList<>();
            roles.add(Role.USER);

            switch (userType) {
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
            user.setEmail(userEmail);
        }

        //UPDATE USER:
        // if access token or name have changed, update them
        user.setName(name);
        user.setEmail(userEmail);

        int roles;
        switch (userType) {
            case FACEBOOK:
                user.setFbAccessToken(accessToken);
                user.setFbUserId(id);
                roles = Role.addRole(user.getRoles(), Role.FACEBOOK);
                break;
            case GOOGLE:
                user.setGoogleAccessToken(accessToken);
                user.setGoogleUserId(id);
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

    private String getUserEmail(String userId, String accessToken) {
        OAuth2RestTemplate rt = getOAuth2RestTemplate(accessToken);
        Map<String, String> responseBody = rt.getForEntity(this.userInfoEndpointUrl + "?fields=email", LinkedHashMap.class).getBody();

        String email = responseBody.get("email");
        if (StringUtils.isEmpty(email)) {
            throw new RuntimeException("Email cannot be retrieved!");
        }

        return email;
    }

    enum UserType {
        FACEBOOK,
        GOOGLE
    }

}
