package me.remind.configuration;

import static me.remind.configuration.SecurityConstants.EXPIRATION_TIME;
import static me.remind.configuration.SecurityConstants.HEADER_STRING;
import static me.remind.configuration.SecurityConstants.SECRET;
import static me.remind.configuration.SecurityConstants.TOKEN_PREFIX;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * By vlad.oltean on 28/07/2018.
 */
public class MyCustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private String redirectUrl = "http://localhost:8080";

    public MyCustomAuthenticationSuccessHandler(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public MyCustomAuthenticationSuccessHandler() {
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //add JWT token on response
        OAuth2Authentication auth = (OAuth2Authentication) authentication;
        UsernamePasswordAuthenticationToken user = (UsernamePasswordAuthenticationToken) auth.getUserAuthentication();
        // TODO: add details to token

        String token = Jwts.builder()
                .setSubject(authentication.getPrincipal().toString())
                //TODO: Refactor
                .setClaims((Map<String, Object>) user.getDetails())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
                .compact();
        response.setStatus(302);
        response.addHeader(HttpHeaders.LOCATION, this.redirectUrl);
        response.addHeader(HttpHeaders.SET_COOKIE, "jwt=" + token + "; Path=/;"); //removed http only
        response.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
    }
}
