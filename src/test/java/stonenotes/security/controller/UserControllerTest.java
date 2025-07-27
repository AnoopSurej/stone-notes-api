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
import stonenotes.common.ApiResponse;
import stonenotes.dto.UserLoginDto;
import stonenotes.dto.UserRegistrationDto;
import stonenotes.exception.EmailAlreadyExistsException;
import stonenotes.security.jwt.JwtTokenProvider;
import stonenotes.service.UserService;

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

        when(userDetailsService.loadUserByUsername(userLoginDto.getEmail())).thenReturn(userDetails);
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn("mocked-jwt-token");

        ResponseEntity<ApiResponse<String>> response = userController.login(userLoginDto);
        ApiResponse<String> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());
        assertEquals("mocked-jwt-token", responseBody.getData());
        assertEquals("Login successful", responseBody.getMessage());
        assertEquals(200, responseBody.getStatus());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername(userLoginDto.getEmail());
        verify(jwtTokenProvider, times(1)).generateToken(userDetails);
    }
}
