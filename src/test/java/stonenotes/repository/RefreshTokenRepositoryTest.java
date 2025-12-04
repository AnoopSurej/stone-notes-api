package stonenotes.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import stonenotes.builders.UserBuilder;
import stonenotes.model.RefreshToken;
import stonenotes.model.User;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RefreshTokenRepositoryTest {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private RefreshToken createRefreshToken(User user, String token, Instant expiryTime) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(expiryTime);
        return refreshToken;
    }

    @Test
    void shouldReturnRefreshTokenIfPresent() {
        User user = UserBuilder.aUser().build();
        testEntityManager.persistAndFlush(user);

        String mockToken = "mock-refresh-token";

        RefreshToken refreshToken = createRefreshToken(
                user,
                mockToken,
                Instant.now()
        );
        testEntityManager.persistAndFlush(refreshToken);

        Optional<RefreshToken> token = refreshTokenRepository.findByToken(mockToken);

        assertThat(token).isPresent();
    }

    @Test
    void shouldReturnEmptyIfTokenNotPresent() {
        User user = UserBuilder.aUser().build();
        testEntityManager.persistAndFlush(user);

        String mockToken = "mock-refresh-token";

        Optional<RefreshToken> token = refreshTokenRepository.findByToken(mockToken);

        assertThat(token).isEmpty();
    }
}
