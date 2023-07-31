package buky.example.reservationsservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidGuestNumberException extends RuntimeException {

    public InvalidGuestNumberException(String msg) {
        super(msg);
    }
    public InvalidGuestNumberException() {
        super("Error! Invalid Guest Number!");
    }
}
