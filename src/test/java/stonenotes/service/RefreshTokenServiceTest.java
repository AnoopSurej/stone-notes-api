package stonenotes.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stonenotes.builders.UserBuilder;
import stonenotes.model.RefreshToken;
import stonenotes.model.User;
import stonenotes.repository.RefreshTokenRepository;
import stonenotes.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import stonenotes.exception.RefreshTokenExpiredException;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void shouldCreateRefreshTokenSuccessfully() {
        Long userId = 1L;
        User user = UserBuilder.aUser().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant beforeCreation = Instant.now();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId);

        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getToken()).isNotEmpty();
        assertThat(UUID.fromString(refreshToken.getToken())).isNotNull();
        assertThat(refreshToken.getExpiryDate()).isAfter(beforeCreation);
        assertThat(refreshToken.getUser()).isEqualTo(user);

        verify(userRepository).findById(userId);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldThrowRuntimeErrorIfUserNotFound() {
        Long userId = 1L;

        assertThatThrownBy(() -> refreshTokenService.createRefreshToken(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldReturnTrueIfTokenIsExpired() {
        Instant now = Instant.now();
        Instant refreshTokenExpiry = now.minusSeconds(10L);
        User user = UserBuilder.aUser().build();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(refreshTokenExpiry);

        boolean result = refreshTokenService.isTokenExpired(refreshToken);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseIfTokenIsNotExpired() {
        Instant now = Instant.now();
        Instant refreshTokenExpiry = now.plusSeconds(10L);
        User user = UserBuilder.aUser().build();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(refreshTokenExpiry);

        boolean result = refreshTokenService.isTokenExpired(refreshToken);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTokenExpiresInOneMillisecond() {
        Instant almostExpired = Instant.now().plusMillis(1);
        User user = UserBuilder.aUser().build();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(almostExpired);

        boolean result = refreshTokenService.isTokenExpired(refreshToken);

        // Token expires in 1ms, still valid (not before current time)
        assertThat(result).isFalse();
    }

    @Test
    void shouldGenerateUniqueTokensForMultipleRequests() {
        Long userId = 1L;
        User user = UserBuilder.aUser().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token1 = refreshTokenService.createRefreshToken(userId);
        RefreshToken token2 = refreshTokenService.createRefreshToken(userId);

        assertThat(token1.getToken()).isNotEqualTo(token2.getToken());
        assertThat(UUID.fromString(token1.getToken())).isNotNull();
        assertThat(UUID.fromString(token2.getToken())).isNotNull();
    }

    @Test
    void shouldAssociateTokenWithCorrectUser() {
        Long userId = 1L;
        User user = UserBuilder.aUser()
                .withEmail("test@example.com")
                .withFirstName("Jane")
                .withLastName("Smith")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId);

        assertThat(refreshToken.getUser()).isNotNull();
        assertThat(refreshToken.getUser()).isEqualTo(user);
        assertThat(refreshToken.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(refreshToken.getUser().getFirstName()).isEqualTo("Jane");
        assertThat(refreshToken.getUser().getLastName()).isEqualTo("Smith");
    }

    @Test
    void shouldFindRefreshTokenByTokenString() {
        String tokenString = "test-token-123";
        User user = UserBuilder.aUser().build();
        RefreshToken expectedToken = new RefreshToken();
        expectedToken.setToken(tokenString);
        expectedToken.setUser(user);
        expectedToken.setExpiryDate(Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(expectedToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken(tokenString);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(tokenString);
        assertThat(result.get().getUser()).isEqualTo(user);
        verify(refreshTokenRepository).findByToken(tokenString);
    }

    @Test
    void shouldReturnEmptyWhenTokenNotFound() {
        String tokenString = "non-existent-token";

        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.empty());

        Optional<RefreshToken> result = refreshTokenService.findByToken(tokenString);

        assertThat(result).isEmpty();
        verify(refreshTokenRepository).findByToken(tokenString);
    }

    @Test
    void shouldReturnTokenWhenNotExpired() {
        User user = UserBuilder.aUser().build();
        RefreshToken token = new RefreshToken();
        token.setToken("valid-token");
        token.setUser(user);
        token.setExpiryDate(Instant.now().plusSeconds(3600)); // Expires in 1 hour

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertThat(result).isEqualTo(token);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void shouldDeleteTokenAndThrowExceptionWhenExpired() {
        User user = UserBuilder.aUser().build();
        RefreshToken token = new RefreshToken();
        token.setToken("expired-token");
        token.setUser(user);
        token.setExpiryDate(Instant.now().minusSeconds(3600)); // Expired 1 hour ago

        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(token))
                .isInstanceOf(RefreshTokenExpiredException.class)
                .hasMessage("Refresh token expired. Please login again");

        verify(refreshTokenRepository).delete(token);
    }
}