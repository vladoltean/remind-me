package me.remind.configuration;

import static me.remind.configuration.SecurityConstants.EXPIRATION_TIME;
import static me.remind.configuration.SecurityConstants.HEADER_STRING;
import static me.remind.configuration.SecurityConstants.SECRET;
import static me.remind.configuration.SecurityConstants.TOKEN_PREFIX;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * By vlad.oltean on 26/07/2018.
 */
public class CustomJwtOauth2AuthenticationFilter extends OAuth2ClientAuthenticationProcessingFilter {

    public CustomJwtOauth2AuthenticationFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);


        //add JWT token on response
        String token = Jwts.builder()
                .setSubject(authResult.getPrincipal().toString())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
                .compact();
        response.addHeader(HEADER_STRING, TOKEN_PREFIX + token);

    }
}
