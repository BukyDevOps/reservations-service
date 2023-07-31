package buky.example.reservationsservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class ActionNotPermittedException extends RuntimeException {
    public ActionNotPermittedException(String msg) {
    }
}
