package me.remind.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.remind.model.User;

/**
 * Created by vlad.oltean on 27/06/2018.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findOneByFbUserId(String fbUserId);

}
