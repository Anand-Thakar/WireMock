package com.example.learningwiremock.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class MovieNotFoundException extends Throwable {
    public MovieNotFoundException(String statusText, WebClientResponseException ex) {
        super(statusText,ex);
    }


    public MovieNotFoundException(Exception ex) {
        super(ex);
    }
}
