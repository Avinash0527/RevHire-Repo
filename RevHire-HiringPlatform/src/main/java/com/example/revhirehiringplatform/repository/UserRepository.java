package com.example.revhirehiringplatform.repository;

import com.example.revhirehiringplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(User.Role role);

    long countByStatus(Boolean status);
}
