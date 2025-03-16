package stonenotes.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import stonenotes.dto.UserRegistrationDto;
import stonenotes.exception.EmailAlreadyExistsException;
import stonenotes.model.User;
import stonenotes.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    private AutoCloseable closeable;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardown() throws Exception {
        closeable.close();
    }

    private User captureUser() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        // Arrange
        String email = "test@example.com";
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(email, "password", "FirstName", "LastName");
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        EmailAlreadyExistsException ex = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.registerUser(userRegistrationDto),
                "EmailAlreadyExistsException should be thrown"
        );

        assertEquals("Email already registered: " + email, ex.getMessage());
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto(email, password, "First", "Last");
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        // Act
        userService.registerUser(userRegistrationDto);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(password);

        User capturedUser = captureUser();
        assertEquals("encodedPassword", capturedUser.getPassword());
        assertEquals(email, capturedUser.getEmail());
    }
}
