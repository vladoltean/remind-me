package me.remind.api;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import me.remind.model.user.User;
import me.remind.repository.UserRepository;

/**
 * Created by vlad.oltean on 27/06/2018.
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> findAll(){
        return userRepository.findAll();
    }

    @GetMapping("me")
    public Principal user(Principal principal) {
        return principal;
    }

}
