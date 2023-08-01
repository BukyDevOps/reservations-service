package buky.example.reservationsservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class DateNotAvailableException extends RuntimeException {
    public DateNotAvailableException(String msg) {
        super(msg);
    }
    public DateNotAvailableException() {
        super("Dates Not available");
    }
}
