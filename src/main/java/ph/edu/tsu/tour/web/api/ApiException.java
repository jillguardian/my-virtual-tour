package ph.edu.tsu.tour.web.api;

@lombok.Data
public class ApiException extends RuntimeException {

    private static final int DEFAULT_CODE = 500;
    private final int code;

    ApiException(String message) {
        this(message, null);
    }

    ApiException(String message, Throwable cause) {
        this(message, cause, ApiException.DEFAULT_CODE);
    }

    ApiException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

}
