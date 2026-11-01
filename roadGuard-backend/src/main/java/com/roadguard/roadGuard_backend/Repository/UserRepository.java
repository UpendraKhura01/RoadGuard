package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String number);

    Optional<User> findByName(String username);

    Optional<User> findByEmail(String identifier);
}