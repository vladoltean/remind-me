package me.remind.graphql;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;

import lombok.AllArgsConstructor;
import me.remind.model.user.User;
import me.remind.service.UserService;

/**
 * By vlad.oltean on 27/06/2018.
 */
@Component
@AllArgsConstructor
public class MutationResolver implements GraphQLMutationResolver {

    private UserService userService;

    @PreAuthorize("hasRole('ROLE_FACEBOOK')")
    public User newUser(String name) {
        return this.userService.newUser(name);
    }

    @PreAuthorize("hasRole('ROLE_GOOGLE')")
    public Boolean deleteUser(long id) {
        return this.userService.deleteUser(id);
    }

}
