package pawa_be.user_auth.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawa_be.user_auth.internal.model.UserAuthModel;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuthModel, Long> {
    UserAuthModel findByEmail(String email);
}

