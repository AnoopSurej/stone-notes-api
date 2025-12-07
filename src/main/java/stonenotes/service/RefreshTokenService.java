package stonenotes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import stonenotes.exception.RefreshTokenExpiredException;
import stonenotes.model.RefreshToken;
import stonenotes.repository.RefreshTokenRepository;
import stonenotes.repository.UserRepository;
import stonenotes.model.User;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${jwt.refreshExpirationMillis}")
    private long refreshTokenDurationMillis;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken createRefreshToken(Long userId) {
        var refreshToken = new RefreshToken();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Instant expiryDate = Instant.now().plusMillis(refreshTokenDurationMillis);
        String token = UUID.randomUUID().toString();

        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(expiryDate);

        return refreshTokenRepository.save(refreshToken);
    }

    public boolean isTokenExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(Instant.now());
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (isTokenExpired(token)) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenExpiredException("Refresh token expired. Please login again");
        }
        return token;
    }
}
