package com.roadguard.roadGuard_backend.Repository;

import com.roadguard.roadGuard_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}