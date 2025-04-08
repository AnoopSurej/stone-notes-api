package stonenotes.common;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Integer status;

    // Default constructor
    public ApiResponse() {}

    // Constructor for success responses with data
    public ApiResponse(T data, String message, Integer status) {
        this.success = true;
        this.message = message;
        this.data = data;
        this.status = status;
    }

    // Constructor for error responses
    public ApiResponse(String message, Integer status) {
        this.success = false;
        this.message = message;
        this.data = null;
        this.status = status;
    }

    public static <T> ApiResponse<T> success(T data, String message, int status) {
        return new ApiResponse<>(data, message, status);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message, 200);
    }

    public static <T> ApiResponse<T> error(String message, int status) {
        return new ApiResponse<>(message, status);
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}