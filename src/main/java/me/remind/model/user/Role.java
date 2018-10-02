package me.remind.model.user;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.CollectionUtils;

/**
 * By vlad.oltean on 04/07/2018.
 */
public enum Role {

    ADMIN("ROLE_ADMIN", 0b00001),
    USER("ROLE_USER", 0b00010),
    FACEBOOK("ROLE_FACEBOOK", 0b00100),
    GOOGLE("ROLE_GOOGLE", 0b01000);

    private String name;
    private int bitmask;

    Role(String name, int bitmask) {
        this.name = name;
        this.bitmask = bitmask;
    }

    public static List<Role> convert(int roles) {
        List<Role> authorities = new ArrayList<>();

        if ((roles & ADMIN.bitmask) != 0) {
            authorities.add(ADMIN);
        }

        if ((roles & USER.bitmask) != 0) {
            authorities.add(USER);
        }

        if ((roles & FACEBOOK.bitmask) != 0) {
            authorities.add(FACEBOOK);
        }

        if ((roles & GOOGLE.bitmask) != 0) {
            authorities.add(GOOGLE);
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

    public static List<GrantedAuthority> convertToAuthorities(int roles) {
        List<Role> rolesList = Role.convert(roles);

        return rolesList.stream()
                .map(role -> new SimpleGrantedAuthority(role.name))
                .collect(Collectors.toList());
    }

    public static int addRole(int originalRole, Role newRoleToAdd){
        List<Role> roles = Role.convert(originalRole);
        if(roles.contains(newRoleToAdd)){
            return originalRole;
        }

        roles.add(newRoleToAdd);
        return Role.getRoles(roles);
    }

}
