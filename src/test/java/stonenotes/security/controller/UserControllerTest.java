package stonenotes.security.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import stonenotes.builders.UserBuilder;
import stonenotes.common.ApiResponse;
import stonenotes.dto.LoginResponseDto;
import stonenotes.dto.RefreshTokenRequestDto;
import stonenotes.dto.UserLoginDto;
import stonenotes.dto.UserRegistrationDto;
import stonenotes.exception.EmailAlreadyExistsException;
import stonenotes.exception.RefreshTokenExpiredException;
import stonenotes.model.RefreshToken;
import stonenotes.model.User;
import stonenotes.security.jwt.JwtTokenProvider;
import stonenotes.service.RefreshTokenService;
import stonenotes.service.UserService;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserController userController;

    @Test
    void testRegisterUser_Success() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
                "test@example.com",
                "password",
                "first",
                "last"
        );

        ResponseEntity<ApiResponse<String>> response = userController.registerUser(userRegistrationDto);
        ApiResponse<String> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());
        assertEquals("Registration successful", responseBody.getMessage());
        assertEquals("User registered successfully", responseBody.getData());
        assertEquals(200, responseBody.getStatus());

        verify(userService,times(1)).registerUser(userRegistrationDto);

    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
                "test@example.com",
                "password",
                "first",
                "last"
        );
        doThrow(new EmailAlreadyExistsException("Email already registered: test@example.com"))
                .when(userService).registerUser(any(UserRegistrationDto.class));

        EmailAlreadyExistsException ex = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userController.registerUser(userRegistrationDto)
        );

        assertEquals("Email already registered: test@example.com", ex.getMessage());
        verify(userService, times(1)).registerUser(userRegistrationDto);
    }

    @Test
    void testLoginUser_Success() {
        UserLoginDto userLoginDto = new UserLoginDto("test@example.com","password");
        UserDetails userDetails = mock(UserDetails.class);
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("mocked-refresh-token");

        when(userDetailsService.loadUserByUsername(userLoginDto.getEmail())).thenReturn(userDetails);
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn("mocked-jwt-token");
        when(userService.getUserIdByEmail(userLoginDto.getEmail())).thenReturn(1L);
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(mockRefreshToken);

        ResponseEntity<ApiResponse<LoginResponseDto>> response = userController.login(userLoginDto);
        ApiResponse<LoginResponseDto> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());

        LoginResponseDto loginResponse = responseBody.getData();
        assertNotNull(loginResponse);
        assertEquals("mocked-jwt-token", loginResponse.getAccessToken());
        assertEquals("mocked-refresh-token", loginResponse.getRefreshToken());
        assertEquals("Bearer", loginResponse.getTokenType());

        assertEquals("Login successful", responseBody.getMessage());
        assertEquals(200, responseBody.getStatus());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername(userLoginDto.getEmail());
        verify(jwtTokenProvider, times(1)).generateToken(userDetails);
        verify(userService, times(1)).getUserIdByEmail(userLoginDto.getEmail());
        verify(refreshTokenService, times(1)).createRefreshToken(1L);
    }

    @Test
    void testRefreshToken_Success() {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto("valid-refresh-token");
        User user = UserBuilder.aUser().withEmail("test@example.com").build();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("valid-refresh-token");
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));

        when(refreshTokenService.findByToken("valid-refresh-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(jwtTokenProvider.generateToken(user)).thenReturn("new-access-token");

        ResponseEntity<ApiResponse<LoginResponseDto>> response = userController.refreshToken(request);
        ApiResponse<LoginResponseDto> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());

        LoginResponseDto loginResponse = responseBody.getData();
        assertNotNull(loginResponse);
        assertEquals("new-access-token", loginResponse.getAccessToken());
        assertEquals("valid-refresh-token", loginResponse.getRefreshToken());
        assertEquals("Bearer", loginResponse.getTokenType());

        assertEquals("Token refreshed", responseBody.getMessage());

        verify(refreshTokenService, times(1)).findByToken("valid-refresh-token");
        verify(refreshTokenService, times(1)).verifyExpiration(refreshToken);
        verify(jwtTokenProvider, times(1)).generateToken(user);
    }

    @Test
    void testRefreshToken_NotFound() {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto("invalid-token");

        when(refreshTokenService.findByToken("invalid-token")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userController.refreshToken(request);
        });

        assertEquals("Refresh token not found", exception.getMessage());
        verify(refreshTokenService, times(1)).findByToken("invalid-token");
        verify(refreshTokenService, never()).verifyExpiration(any());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void testRefreshToken_Expired() {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto("expired-token");
        User user = UserBuilder.aUser().build();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("expired-token");
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().minusSeconds(3600));

        when(refreshTokenService.findByToken("expired-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken))
                .thenThrow(new RefreshTokenExpiredException("Refresh token expired. Please login again"));

        RefreshTokenExpiredException exception = assertThrows(RefreshTokenExpiredException.class, () -> {
            userController.refreshToken(request);
        });

        assertEquals("Refresh token expired. Please login again", exception.getMessage());
        verify(refreshTokenService, times(1)).findByToken("expired-token");
        verify(refreshTokenService, times(1)).verifyExpiration(refreshToken);
        verify(jwtTokenProvider, never()).generateToken(any());
    }
}
