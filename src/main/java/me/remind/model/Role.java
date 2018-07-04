package me.remind.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.CollectionUtils;

/**
 * By vlad.oltean on 04/07/2018.
 */
public enum Role {

    ADMIN("ROLE_ADMIN", 0b001),
    USER("ROLE_USER", 0b010);

    private String name;
    private int bitmask;

    Role(String name, int bitmask) {
        this.name = name;
        this.bitmask = bitmask;
    }

    public static List<GrantedAuthority> convert(int roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if ((roles & ADMIN.bitmask) != 0) {
            authorities.add(new SimpleGrantedAuthority(ADMIN.name));
        }

        if ((roles & USER.bitmask) != 0) {
            authorities.add(new SimpleGrantedAuthority(USER.name));
        }

        return authorities;
    }

    public static int getRoles(List<Role> roles){
        int rolesAsInt = 0;

        if(CollectionUtils.isEmpty(roles)){
            throw new IllegalArgumentException("Roles list is empty or null. Could not convert.");
        }

        for(Role role: roles){
            rolesAsInt += role.bitmask;
        }
        return rolesAsInt;
    }

}
