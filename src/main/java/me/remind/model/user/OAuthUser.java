package me.remind.model.user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * By vlad.oltean on 02/10/2018.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuthUser {

    @Id
    @GeneratedValue
    private Long id;

    private String authServerUserId;
    private String authServerAccessToken;

}
