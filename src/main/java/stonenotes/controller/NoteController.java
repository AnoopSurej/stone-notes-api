package stonenotes.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import stonenotes.common.ApiResponse;
import stonenotes.dto.CreateNoteDto;
import stonenotes.dto.NoteResponseDto;
import stonenotes.dto.UpdateNoteDto;
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

    @GetMapping("/notes")
    public ResponseEntity<ApiResponse<Page<NoteResponseDto>>> getNotes(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String email = authentication.getName();
        Long userId = userService.getUserIdByEmail(email);

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<NoteResponseDto> notesPage = noteService.findNotesByUserId(userId, pageable);

        ApiResponse<Page<NoteResponseDto>> response = ApiResponse.success(notesPage, "Notes retrieved successfully", 200);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/notes/{noteId}")
    public ResponseEntity<ApiResponse<NoteResponseDto>> getNote(
            Authentication authentication,
            @PathVariable Long noteId) {
        String email = authentication.getName();
        Long userId = userService.getUserIdByEmail(email);

        NoteResponseDto note = noteService.findNoteByIdAndUserId(noteId, userId);

        ApiResponse<NoteResponseDto> response = ApiResponse.success(note, "Note retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/notes/{noteId}")
    public ResponseEntity<ApiResponse<NoteResponseDto>> updateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody UpdateNoteDto updateNoteDto,
            Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserIdByEmail(email);

        NoteResponseDto updatedNote = noteService.updateNote(noteId, updateNoteDto, userId);

        ApiResponse<NoteResponseDto> response = ApiResponse.success(updatedNote, "Note updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable Long noteId,
            Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserIdByEmail(email);

        noteService.deleteNote(noteId, userId);

        ApiResponse<Void> response = ApiResponse.success(null, "Note deleted successfully");
        return ResponseEntity.ok(response);
    }
}
