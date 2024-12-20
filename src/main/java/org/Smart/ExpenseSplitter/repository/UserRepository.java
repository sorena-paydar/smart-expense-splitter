package org.Smart.ExpenseSplitter.repository;

import org.Smart.ExpenseSplitter.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link UserEntity} entities.
 * Provides methods for common operations like finding users by username or email.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Find a user by their username.
     *
     * @param username the username of the user to find
     * @return an Optional containing the found UserEntity, or empty if no user is found
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Find a user by their email address.
     *
     * @param email the email of the user to find
     * @return an Optional containing the found UserEntity, or empty if no user is found
     */
    Optional<UserEntity> findByEmail(String email);
}
