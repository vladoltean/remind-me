package me.remind.graphql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;

import lombok.AllArgsConstructor;
import me.remind.model.User;
import me.remind.repository.UserRepository;

/**
 * By vlad.oltean on 27/06/2018.
 */
@Component
@AllArgsConstructor
public class MutationResolver implements GraphQLMutationResolver {

    private UserRepository userRepository;

    public User newUser(String name) {
        User user = new User();
        user.setName(name);
        return this.userRepository.save(user);
    }

    public Boolean deleteUser(long id) {
        this.userRepository.delete(id);
        return true;

    }

}
