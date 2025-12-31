package stonenotes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import stonenotes.common.ApiResponse;

import java.util.HashMap;
import java.util.Map;
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotNotFoundException(NoteNotFoundException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error: ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ApiResponse<Map<String, String>> response = ApiResponse.error("Validation failed", HttpStatus.BAD_REQUEST.value());
        response.setData(errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
