package es.grupo8.backend.dto;

public class AssignmentResponseDTO {
    private boolean success;
    private String message;
    private Object data;
    private String errorType;

    public AssignmentResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AssignmentResponseDTO(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public AssignmentResponseDTO(boolean success, String message, String errorType) {
        this.success = success;
        this.message = message;
        this.errorType = errorType;
    }

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
}