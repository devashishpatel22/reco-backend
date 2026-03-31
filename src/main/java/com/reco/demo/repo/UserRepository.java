package com.reco.demo.repo;

import com.reco.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Used by JwtFilter and AuthController to find a user
     * based on the 'subject' inside the JWT token.
     */
    Optional<User> findByUsername(String username);

    /**
     * Used during Signup to prevent duplicate usernames
     * without needing to fetch the full User entity.
     */
    Boolean existsByUsername(String username);
}