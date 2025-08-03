package stonenotes.builders;

import stonenotes.dto.NoteResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class NoteResponseDtoBuilder {
    private Long id = ThreadLocalRandom.current().nextLong(1, 10000);
    private String title = "Note Title " + UUID.randomUUID().toString().substring(0, 8);
    private String content = "Note content " + UUID.randomUUID().toString().substring(0, 8);
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    private NoteResponseDtoBuilder() {}

    public static NoteResponseDtoBuilder aNoteResponseDto() { return new NoteResponseDtoBuilder(); }

    public NoteResponseDtoBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public NoteResponseDtoBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public NoteResponseDtoBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public NoteResponseDtoBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public NoteResponseDtoBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public NoteResponseDto build() {
        NoteResponseDto dto = new NoteResponseDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setContent(content);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);

        return dto;
    }
}
