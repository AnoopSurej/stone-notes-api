package stonenotes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateNoteDto {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @Size(max = 10000, message = "Content must be less than 10000 characters")
    private String content;

    public UpdateNoteDto() {}

    public UpdateNoteDto(String title, String content) {
        this.title = title;
        this.content = content;
    }

}
