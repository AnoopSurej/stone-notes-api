package stonenotes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stonenotes.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    public User findByEmail(String email);
    User findByUsername(String username);
}
