package stonenotes.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class NoteResponseDto {
    private Long id;
    private String title;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;

    public NoteResponseDto() {}

    public NoteResponseDto(Long id, String title, String content, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
