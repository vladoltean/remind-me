package me.remind.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import me.remind.model.user.OAuthUser;
import me.remind.model.user.Role;
import me.remind.model.user.User;
import me.remind.repository.OAuthUserRepository;
import me.remind.repository.UserRepository;

/**
 * By vlad.oltean on 06/07/2018.
 */
@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private OAuthUserRepository oAuthUserRepository;

    public User newUser(String name) {
        User user = new User();
        user.setName(name);
        return this.userRepository.save(user);
    }

    public Boolean deleteUser(long id) {
        this.userRepository.deleteById(id);
        return true;
    }


    /**
     * Save user to database if does not exist.
     * It also updates his name and access token for the specific login method used, if exists.
     *
     * @param id          - from the principal, which may be either of the oauth server ids.
     * @param accessToken - the access token specific to the oauth server used.
     * @param name        - name of the user.
     * @return - saved Entity.
     */
    @Transactional
    //TODO: Refactor into multiple methods
    public User handleAuthenticationForUser(String id, String accessToken, String name, String userEmail, UserType userType) {
        User user;

        // search by email
        // get users email in initial call and add it to the Principal object :)
        user = userRepository.findOneByEmail(userEmail);

        if (user == null) {
            //if no user was found by email, search by id.
            // Refuse users that do not have email?
            switch (userType) {
                case FACEBOOK:
                    user = userRepository.findOneByFacebookUser_AuthServerUserId(id);
                    break;
                case GOOGLE:
                    user = userRepository.findOneByGoogleUserId(id);
                    break;
                default:
                    throw new RuntimeException("User type is not supported!");
            }
        }


        //CREATE USER
        if (user == null) {
            user = new User();

            List<Role> roles = new ArrayList<>();
            roles.add(Role.USER);

            switch (userType) {
                case FACEBOOK:
                    OAuthUser fbUser = Optional.ofNullable(user.getFacebookUser())
                            .orElse(new OAuthUser());
                    fbUser.setAuthServerUserId(id);
                    oAuthUserRepository.save(fbUser);
                    user.setFacebookUser(fbUser);
                    roles.add(Role.FACEBOOK);
                    break;
                case GOOGLE:
                    user.setGoogleUserId(id);
                    roles.add(Role.GOOGLE);
                    break;
                default:
                    throw new RuntimeException("User type is not supported!");
            }
            user.setRoles(Role.getRoles(roles));
            user.setEmail(userEmail);
        }

        //UPDATE USER:
        // if access token or name have changed, update them
        user.setName(name);
        user.setEmail(userEmail);

        int roles;
        switch (userType) {
            case FACEBOOK:
                //TODO: extract this separately
                OAuthUser fbUser = Optional.ofNullable(user.getFacebookUser())
                        .orElse(new OAuthUser());
                fbUser.setAuthServerUserId(id);
                fbUser.setAuthServerAccessToken(accessToken);
                oAuthUserRepository.save(fbUser);
                user.setFacebookUser(fbUser);
                roles = Role.addRole(user.getRoles(), Role.FACEBOOK);
                break;
            case GOOGLE:
                user.setGoogleAccessToken(accessToken);
                user.setGoogleUserId(id);
                roles = Role.addRole(user.getRoles(), Role.GOOGLE);
                break;
            default:
                throw new RuntimeException("User type is not supported!");
        }
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public enum UserType {
        FACEBOOK,
        GOOGLE
    }


}
