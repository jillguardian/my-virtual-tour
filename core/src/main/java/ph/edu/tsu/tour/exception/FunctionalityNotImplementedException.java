package ph.edu.tsu.tour.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED)
public class FunctionalityNotImplementedException extends RuntimeException {

    public FunctionalityNotImplementedException(String message) {
        super(message);
    }

}
