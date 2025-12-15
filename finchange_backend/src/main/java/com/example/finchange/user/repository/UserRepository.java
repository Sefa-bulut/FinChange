package com.example.finchange.user.repository;

import com.example.finchange.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> , JpaSpecificationExecutor<User> {

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    boolean existsByPersonnelCode(String personnelCode);
}