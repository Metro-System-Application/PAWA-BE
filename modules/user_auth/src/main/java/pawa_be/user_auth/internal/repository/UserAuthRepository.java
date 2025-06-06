package pawa_be.user_auth.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawa_be.user_auth.internal.model.UserAuthModel;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuthModel, String> {
    UserAuthModel findByEmail(String email);
    Optional<UserAuthModel> findByGoogleId(String googleId);
    Optional<UserAuthModel> findByUserIdAndGoogleIdIsNotNull(String userId);
}

