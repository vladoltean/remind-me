package me.remind.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.remind.model.user.OAuthUser;

/**
 * By vlad.oltean on 02/10/2018.
 */
@Repository
public interface OAuthUserRepository extends JpaRepository<OAuthUser, Long> {

}
