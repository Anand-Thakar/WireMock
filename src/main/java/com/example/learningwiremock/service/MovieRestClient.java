package com.example.learningwiremock.service;

import com.example.learningwiremock.constants.MovieConstants;
import com.example.learningwiremock.exception.MovieNotCreated;
import com.example.learningwiremock.exception.MovieNotFoundException;
import com.example.learningwiremock.model.Movie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MovieRestClient {


    private WebClient webClient;


    public MovieRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    //http://localhost:8081/movieservice/v1/allMovies
    public List<Movie> getAllMovies() throws MovieNotFoundException {
       try {List<Movie> block = webClient.get()
               .uri(MovieConstants.GET_ALL_MOVIES)
               .retrieve().bodyToFlux(Movie.class)
               .collect(Collectors.toList())
               .block();

           return block;
       } catch (WebClientResponseException ex){
           log.error("MovieNotFoundException in retrieveMovieById. Status code {} and the message is {}", ex.getStatusCode(), ex.getResponseBodyAsString());
           throw new MovieNotFoundException(ex.getStatusText(), ex);

       }
    }

    public Movie getMovieById(Long id) throws MovieNotFoundException {


        try {
            Movie block = webClient.get()
                    .uri(MovieConstants.GET_MOVIE_BY_ID, id)
                    .retrieve().bodyToMono(Movie.class)
                    .block();
            return block;
        } catch (WebClientResponseException ex) {
            log.error("MovieNotFoundException in retrieveMovieById. Status code {} and the message is {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MovieNotFoundException(ex.getStatusText(), ex);
        }

    }

    public List<Movie> getMovieByName(String movieName) throws MovieNotFoundException {


        try {
            List<Movie> movieName1 = webClient.get().uri(uriBuilder -> uriBuilder
                            .path(MovieConstants.Get_MOVIE_BY_NAME)
                            .queryParam("movie_name", movieName)
                            .build())
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collect(Collectors.toList())
                    .block();

            return movieName1;

        } catch (WebClientResponseException ex) {

            log.error("No movies with the name {} found", movieName);
            throw new MovieNotFoundException(ex.getResponseBodyAsString(), ex);
        }
    }

    public Movie createMovie(Movie movie) throws MovieNotCreated {
        try {
            return webClient.post()
                    .uri(MovieConstants.CREATE_MOVIE)
                    .bodyValue(movie)
                    .retrieve()
                    .bodyToMono(Movie.class).block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException in createMovie. Status code {} and message is {} ",ex.getStatusCode().value(),ex.getResponseBodyAsString());
            throw new MovieNotCreated(ex,ex.getResponseBodyAsString());
        }

    }

    public Movie updateMovie(Integer movieId, Movie movie) throws MovieNotCreated {
        try {
            return webClient.put()
                    .uri(MovieConstants.UPDATE_MOVIE, movieId)
                    .bodyValue(movie)
                    .retrieve()
                    .bodyToMono(Movie.class).block();
        } catch (WebClientResponseException ex) {
            log.error("Not able to update movie");
            throw new MovieNotCreated(ex, ex.getResponseBodyAsString());
        }

    }

    public String deleteMovie(Integer movieId) throws MovieNotCreated {
        try {
            return webClient.delete()
                    .uri(MovieConstants.GET_MOVIE_BY_ID, movieId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Not able to update movie");
            throw new MovieNotCreated(ex, ex.getResponseBodyAsString());
        }

    }

    public List<Movie> getMovieByYear(Integer year) throws MovieNotFoundException {

        try {
            return webClient.get().uri(uriBuilder -> uriBuilder
                            .path(MovieConstants.GET_MOVIE_BY_YEAR)
                            .queryParam("year", year)
                            .build())
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collect(Collectors.toList())
                    .block();
        } catch (WebClientResponseException ex) {

            log.error("No movies with the year {} found, and message is {}", year, ex.getResponseBodyAsString());
            throw new MovieNotFoundException(ex);
        }
    }

    public String deleteMovieByName(String movieName) throws MovieNotFoundException {

        try {
           webClient.delete().uri(uriBuilder -> uriBuilder
                            .path(MovieConstants.Get_MOVIE_BY_NAME)
                            .queryParam("movie_name", movieName)
                            .build())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException ex) {

            log.error("Movie deleted with the name {}", movieName);
            throw new MovieNotFoundException(ex.getResponseBodyAsString(), ex);
        }

        return "Movie Deleted Successfully";
    }
}
