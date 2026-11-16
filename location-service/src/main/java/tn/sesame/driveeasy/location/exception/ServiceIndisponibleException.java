package tn.sesame.driveeasy.location.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceIndisponibleException extends RuntimeException {
    public ServiceIndisponibleException(String message) {
        super(message);
    }
}
