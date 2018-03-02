package ph.edu.tsu.tour.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.GONE)
public class ExpiredResourceException extends RuntimeException {

    public ExpiredResourceException(String message) {
        super(message);
    }

    public ExpiredResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
