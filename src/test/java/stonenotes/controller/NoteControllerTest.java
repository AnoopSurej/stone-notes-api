package stonenotes.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import stonenotes.common.ApiResponse;
import stonenotes.dto.CreateNoteDto;
import stonenotes.dto.NoteResponseDto;
import stonenotes.service.NoteService;
import stonenotes.service.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private NoteService noteService;

    @InjectMocks
    private NoteController noteController;

    @Test
    void shouldCreateNoteForValidUser() {
        // Given
        String email = "user@example.com";
        Long userId = 1L;
        CreateNoteDto dto = new CreateNoteDto("Test Note","Test Content");
        LocalDateTime now = LocalDateTime.now();
        NoteResponseDto expectedResponse = new NoteResponseDto(userId, dto.getTitle(), dto.getContent(), now, now);
        Authentication auth = mock(Authentication.class);

        when(auth.getName()).thenReturn(email);
        when(userService.getUserIdByEmail(email)).thenReturn(userId);
        when(noteService.createNote(dto, userId)).thenReturn(expectedResponse);

        ResponseEntity<ApiResponse<NoteResponseDto>> response = noteController.createNote(dto, auth);
        ApiResponse<NoteResponseDto> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(201, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());
        assertEquals(responseBody.getData().getTitle(), "Test Note");
        assertEquals(responseBody.getData().getContent(), "Test Content");
        assertEquals(responseBody.getData().getCreatedAt(), now);
        assertEquals(responseBody.getData().getUpdatedAt(), now);

        verify(userService, times(1)).getUserIdByEmail(email);
        verify(noteService, times(1)).createNote(dto, userId);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Rare case: Should be caught by authentication filter
        // Given
        CreateNoteDto dto = new CreateNoteDto("Title", "Content");
        Authentication auth = mock(Authentication.class);

        when(auth.getName()).thenReturn("nonexistent@example.com");
        when(userService.getUserIdByEmail("nonexistent@example.com")).thenThrow(new UsernameNotFoundException("User not found"));

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> noteController.createNote(dto, auth)
        );

        assertEquals("User not found", ex.getMessage());

        verify(userService, times(1)).getUserIdByEmail("nonexistent@example.com");
        verify(noteService, never()).createNote(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenNoteServiceFails() {
        //Given
        CreateNoteDto dto = new CreateNoteDto("Test Note", "Test Content");
        Authentication auth = mock(Authentication.class);
        String email = "user@example.com";
        Long userId = 1L;

        when(auth.getName()).thenReturn(email);
        when(userService.getUserIdByEmail(email)).thenReturn(userId);
        when(noteService.createNote(dto, userId)).thenThrow(
                new RuntimeException("Service temporarily unavailable")
        );

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> noteController.createNote(dto, auth)
        );

        assertEquals(ex.getMessage(), "Service temporarily unavailable");
        verify(userService, times(1)).getUserIdByEmail(email);
        verify(noteService, times(1)).createNote(dto, userId);
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationNameIsNull() {
        // Rare case: Custom authentication implementation returns null
        // Given
        CreateNoteDto dto = new CreateNoteDto("Test Note", "Test Content");
        Authentication auth = mock(Authentication.class);

        when(auth.getName()).thenReturn(null);
        when(userService.getUserIdByEmail(null))
                .thenThrow(new UsernameNotFoundException("User not found"));

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> noteController.createNote(dto, auth)
        );

        assertEquals("User not found", ex.getMessage());
        verify(userService, times(1)).getUserIdByEmail(null);
        verify(noteService, never()).createNote(any(), any());
    }
}
