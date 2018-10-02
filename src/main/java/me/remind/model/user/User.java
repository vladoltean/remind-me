package me.remind.model.user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by vlad.oltean on 27/06/2018.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private int roles;

    private String email;

    //** OAUTH2 properties

    @OneToOne
    private OAuthUser facebookUser;

    //TODO: Move to separate entity?
    private String googleUserId;
    private String googleAccessToken;




}
