package ph.edu.tsu.tour.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FAILED_DEPENDENCY)
public class FailedDependencyException extends RuntimeException {

    public FailedDependencyException(String message) {
        super(message);
    }

    public FailedDependencyException(String message, Throwable cause) {
        super(message, cause);
    }

}
