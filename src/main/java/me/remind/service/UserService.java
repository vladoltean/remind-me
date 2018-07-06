package me.remind.service;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import me.remind.model.User;
import me.remind.repository.UserRepository;

/**
 * By vlad.oltean on 06/07/2018.
 */
@Service
@AllArgsConstructor
public class UserService {

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
