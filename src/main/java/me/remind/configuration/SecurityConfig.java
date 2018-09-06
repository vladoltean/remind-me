package me.remind.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CompositeFilter;

import lombok.Getter;
import me.remind.repository.UserRepository;

/**
 * By vlad.oltean on 03/07/2018.
 */
@Configuration
@EnableOAuth2Client
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    OAuth2ClientContext oAuth2ClientContext;

    @Autowired
    UserRepository userRepository;

    @Override //todo: add jwt authorization filter
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/**")
                .authorizeRequests() //todo permission based access to graphql resources.. hmm
                .antMatchers("/", "/login**", "/webjars/**", "/error**", "/user", "/graphiql**")
                .permitAll()
                .antMatchers("/graphql").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class)
                .logout().logoutSuccessUrl("/").permitAll()
                .and()
                .csrf().disable();
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);;

    }

    @Bean
    @ConfigurationProperties("facebook")
    public ClientResources facebook() {
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("google")
    public ClientResources google() {
        return new ClientResources();
    }

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter oAuth2ClientContextFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(oAuth2ClientContextFilter);
        registration.setOrder(-100);
        return registration;
    }

    private Filter ssoFilter(){
        CompositeFilter compositeFilter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(ssoFilter(facebook(), "/login/facebook"));
        filters.add(ssoFilter(google(), "/login/google"));
        compositeFilter.setFilters(filters);
        return compositeFilter;
    }

    private Filter ssoFilter(ClientResources client, String path) {
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);

        OAuth2RestTemplate template = new OAuth2RestTemplate(client.getClient(), oAuth2ClientContext);
        filter.setRestTemplate(template);

        MyUserInfoTokenServices userInfoTokenServices = new MyUserInfoTokenServices(
                        client.getResource().getUserInfoUri(), client.getClient().getClientId(), userRepository);

        userInfoTokenServices.setRestTemplate(template);
        filter.setTokenServices(userInfoTokenServices);

        filter.setAuthenticationSuccessHandler(new MyCustomAuthenticationSuccessHandler());

        return filter;
    }

    @Getter
    private static class ClientResources {

        @NestedConfigurationProperty
        private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

        @NestedConfigurationProperty
        private ResourceServerProperties resource = new ResourceServerProperties();
    }
}
