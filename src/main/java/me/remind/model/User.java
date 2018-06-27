package me.remind.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;

/**
 * Created by vlad.oltean on 27/06/2018.
 */
@Entity
@Data
public class User {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

}
