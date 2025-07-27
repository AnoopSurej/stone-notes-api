package stonenotes.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import stonenotes.common.ApiResponse;
import stonenotes.dto.CreateNoteDto;
import stonenotes.dto.NoteResponseDto;
import stonenotes.service.NoteService;
import stonenotes.service.UserService;

@RestController
@RequestMapping("/api")
public class NoteController {
    private final UserService userService;
    private final NoteService noteService;

    public NoteController(UserService userService, NoteService noteService) {
        this.userService = userService;
        this.noteService = noteService;
    }

    @PostMapping("/notes")
    public ResponseEntity<ApiResponse<NoteResponseDto>> createNote(@Valid @RequestBody CreateNoteDto createNoteDto, Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserIdByEmail(email);
        NoteResponseDto noteResponseDto = noteService.createNote(createNoteDto, userId);

        ApiResponse<NoteResponseDto> response = ApiResponse.success(noteResponseDto, "Note created successfully", 201);
        return ResponseEntity.status(201).body(response);
    }
}
