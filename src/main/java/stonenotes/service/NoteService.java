package stonenotes.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import stonenotes.dto.CreateNoteDto;
import stonenotes.dto.NoteResponseDto;
import stonenotes.exception.NoteNotFoundException;
import stonenotes.model.Note;
import stonenotes.model.User;
import stonenotes.repository.NoteRepository;
import stonenotes.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteService(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public NoteResponseDto createNote(CreateNoteDto createNoteDto, Long userId) {
        if(createNoteDto.getTitle() == null || createNoteDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Note note = new Note();
        note.setTitle(createNoteDto.getTitle());
        note.setContent(createNoteDto.getContent());
        note.setUser(user);

        Note savedNote = noteRepository.save(note);

        return convertToResponseDto(savedNote);
    }

    public List<NoteResponseDto> findNotesByUserId(Long userId) {
        List<Note> notes = noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notes.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public Page<NoteResponseDto> findNotesByUserId(Long userId, Pageable pageable) {
        Page<Note> notePage = noteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notePage.map(this::convertToResponseDto);
    }

    public NoteResponseDto findNoteByIdAndUserId(Long id, Long userId) {
        Note note = noteRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new NoteNotFoundException("Note not found"));

        return convertToResponseDto(note);
    }

    private NoteResponseDto convertToResponseDto(Note note) {
        NoteResponseDto dto = new NoteResponseDto();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());

        return dto;
    }
}
