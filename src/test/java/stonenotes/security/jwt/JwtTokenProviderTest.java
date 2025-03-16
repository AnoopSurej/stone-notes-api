package stonenotes.security.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {
    @Spy
    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetails userDetails;

    private final String username = "test@example.com";

    @BeforeEach
    void setup() {
        String testSecret = "thisIsATestSecretKeyWhichNeedsToBeLongEnoughForHS256Algorithm";
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKeyString", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "expirationTime", 86400000L);
    }

    @Test
    void testGenerateToken() {
        when(userDetails.getUsername()).thenReturn(username);
        String token = jwtTokenProvider.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUsername() {
        when(userDetails.getUsername()).thenReturn(username);
        String token = jwtTokenProvider.generateToken(userDetails);

        String extractedUsername = jwtTokenProvider.extractUsername(token);

        assertEquals("test@example.com", extractedUsername);
    }

    @Test
    void testValidateToken_Valid() {
        when(userDetails.getUsername()).thenReturn(username);
        String token = jwtTokenProvider.generateToken(userDetails);

        boolean isValid = jwtTokenProvider.validateToken(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidUsername() {
        when(userDetails.getUsername()).thenReturn(username);
        String token = jwtTokenProvider.generateToken(userDetails);
        UserDetails differentUser = mock(UserDetails.class);
        when(differentUser.getUsername()).thenReturn("different@example.com");

        boolean isValid = jwtTokenProvider.validateToken(token, differentUser);

        assertFalse(isValid);
    }

    @Test
    void testIsTokenExpired_NotExpired() {
        when(userDetails.getUsername()).thenReturn(username);
        String token = jwtTokenProvider.generateToken(userDetails);

        boolean isTokenExpired = jwtTokenProvider.isTokenExpired(token);

        assertFalse(isTokenExpired);
    }

    @Test
    public void testIsTokenExpired_Expired() {
        Date pastDate = new Date(System.currentTimeMillis() - 10000L);

        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getExpiration()).thenReturn(pastDate);
        doReturn(mockClaims).when(jwtTokenProvider).getClaims(anyString());

        boolean isExpired = jwtTokenProvider.isTokenExpired("dummy-token");

        assertTrue(isExpired);
    }

    @Test
    void testGetClaims() {
        when(userDetails.getUsername()).thenReturn(username);
        String token = jwtTokenProvider.generateToken(userDetails);

        Claims claims = jwtTokenProvider.getClaims(token);

        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void testGetClaims_InvalidToken() {
        String invalidToken = "invalid-token";

        assertThrows(Exception.class, () -> jwtTokenProvider.getClaims(invalidToken));
    }
}
