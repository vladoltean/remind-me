package me.remind.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * By vlad.oltean on 16/10/2018.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties("web")
public class WebProperties {

    private String url;

}
