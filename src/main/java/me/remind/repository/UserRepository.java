package me.remind.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.remind.model.user.User;

/**
 * Created by vlad.oltean on 27/06/2018.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findOneByFacebookUser_AuthServerUserId(String fbUserId);
    User findOneByGoogleUserId(String googleUserId);
    User findOneByEmail(String email);

}
