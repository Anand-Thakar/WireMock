package com.example.learningwiremock.service;

import com.example.learningwiremock.exception.MovieNotCreated;
import com.example.learningwiremock.exception.MovieNotFoundException;
import com.example.learningwiremock.model.Movie;
import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static com.example.learningwiremock.constants.MovieConstants.CREATE_MOVIE;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {"moviesapp.baseUrl=http://localhost:8091"})
@ExtendWith(WireMockExtension.class)
public class MoviesRestClientWireMockExtension {


    @InjectServer
    WireMockServer wireMockServer;

    @ConfigureWireMock
    Options options = wireMockConfig().
            port(8091)
            .notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));

    @Autowired
    MovieRestClient movieRestClient;

    @Test
    void getAllMovies_any_url() throws MovieNotFoundException {

        //given
        stubFor(get(anyUrl())
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("all-movies.json")));

        //when
        List<Movie> movieList = movieRestClient.getAllMovies();
        System.out.println("movieList : " + movieList);

        //then return data from our file
        assertTrue(movieList.size() > 0);
    }

    @Test
    void deleteMovie_createMovieAndThenDelete_dynamic() throws MovieNotCreated {
        stubFor(post(CREATE_MOVIE)
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("createMovie-dynamic.json")
                        .withTransformers("response-template")));

        Movie movie = new Movie("chor nikal ke bhaga", 88l, "chor", LocalDate.of(1997, 2, 13), 2023);
        System.out.println(movie);
        Movie movie1 = movieRestClient.createMovie(movie);

        String expectedResponse = "Movie Deleted Successfully";

        stubFor(delete(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(expectedResponse)));


        String deleteResponse = movieRestClient.deleteMovie(movie1.getMovie_id().intValue());
        assertEquals(expectedResponse, deleteResponse);
    }



}
