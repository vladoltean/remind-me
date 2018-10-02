package me.remind.graphql;

import java.util.List;

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;

import lombok.AllArgsConstructor;
import me.remind.model.user.User;
import me.remind.repository.UserRepository;

/**
 * By vlad.oltean on 27/06/2018.
 */
@Component
@AllArgsConstructor
public class QueryResolver implements GraphQLQueryResolver {

    private UserRepository userRepository;

    public List<User> findAllUsers() {
        return this.userRepository.findAll();
    }

    public Long countUsers() {
        return this.userRepository.count();
    }

}
