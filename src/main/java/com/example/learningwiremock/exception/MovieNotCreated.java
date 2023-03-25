package com.example.learningwiremock.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class MovieNotCreated extends Throwable {
    public MovieNotCreated(WebClientResponseException ex, String responseBodyAsString) {
        super(ex);
    }


}
