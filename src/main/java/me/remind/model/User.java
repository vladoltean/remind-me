package me.remind.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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

    //** OAUTH2 properties
    //TODO: Move to separate entity?
    private String fbUserId;
    private String fbAccessToken;


    private String googleUserId;
    private String googleAccessToken;




}
