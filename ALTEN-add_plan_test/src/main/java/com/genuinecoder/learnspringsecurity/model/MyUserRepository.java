package com.genuinecoder.learnspringsecurity.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MyUserRepository extends JpaRepository<MyUser, Long> {

    Optional<MyUser> findByEmail(String email);
    List<MyUser> findByRole(MyUser.Role role);
}
