package stonenotes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stonenotes.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByUsername(String username);
    boolean existsByEmail(String email);
}
