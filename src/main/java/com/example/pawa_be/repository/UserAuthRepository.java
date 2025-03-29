package com.example.pawa_be.repository;

import com.example.pawa_be.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    UserAuth findByEmail(String email);
}
