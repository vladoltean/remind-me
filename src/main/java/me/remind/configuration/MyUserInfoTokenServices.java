package me.remind.configuration;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

import me.remind.model.user.Role;
import me.remind.model.user.User;
import me.remind.service.UserService;

/**
 * By vlad.oltean on 04/07/2018.
 */
public class MyUserInfoTokenServices extends UserInfoTokenServices {

    private UserService userService;
    private String userInfoEndpointUrl;
    private String clientId;
    private String tokenType = DefaultOAuth2AccessToken.BEARER_TYPE;

    private OAuth2RestTemplate restTemplate;

    public MyUserInfoTokenServices(String userInfoEndpointUrl, String clientId, UserService userService) {
        super(userInfoEndpointUrl, clientId);
        this.clientId = clientId;
        this.userInfoEndpointUrl = userInfoEndpointUrl;
        this.userService = userService;


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

        User user = userService.handleAuthenticationForUser(principal, accessToken, name, getUserEmail(principal, accessToken), getUserType());

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


    private UserService.UserType getUserType() {
        if (this.userInfoEndpointUrl.contains("facebook")) {
            return UserService.UserType.FACEBOOK;
        }
        if (this.userInfoEndpointUrl.contains("google")) {
            return UserService.UserType.GOOGLE;
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


}
