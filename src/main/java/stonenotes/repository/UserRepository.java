package stonenotes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stonenotes.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    boolean existsByEmail(String email);
}
