package com.example.learningwiremock.service;

import com.example.learningwiremock.exception.MovieNotCreated;
import com.example.learningwiremock.exception.MovieNotFoundException;
import com.example.learningwiremock.model.Movie;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.time.LocalDate;

import static com.example.learningwiremock.constants.MovieConstants.Get_MOVIE_BY_NAME;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWireMock(port = 8090)
@TestPropertySource(properties = {"moviesapp.baseUrl=http://localhost:8090"})
public class MoviesRestClientServerFaultTest {

    @Autowired
    MovieRestClient movieRestClient;

    WebClient webClient;
    TcpClient tcpClient = TcpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(5))
                    .addHandlerLast(new WriteTimeoutHandler(5)));

    @BeforeEach
    void setUp() {
        //webClient with timeout configuration
        webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl("http://localhost:8090").build();

        stubFor(any(anyUrl()).willReturn(aResponse().proxiedFrom("http://localhost:8081")));
    }

    @Test
    void getAllMovies_serverError() {

        //given
        stubFor(get(anyUrl())
                .willReturn(serverError()));

        //then return data from our file
        assertThrows(MovieNotFoundException.class, () -> movieRestClient.getAllMovies());
    }

    @Test
    void getAllMovies_specific_getUrl() {

        //given
        stubFor(get(anyUrl())
                .willReturn(serverError()
                        .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .withBody("Service unavailable")));

        MovieNotFoundException movieNotFoundException = assertThrows(MovieNotFoundException.class, () -> movieRestClient.getAllMovies());
        //then return data from our file
        assertTrue(movieNotFoundException.getMessage().equalsIgnoreCase("Service unavailable"));
    }

    @Test
    void getAllMovies_fault_response() {

        //given
        stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withFault(Fault.EMPTY_RESPONSE)));

        Exception exception = assertThrows(Exception.class, () -> movieRestClient.getAllMovies());
        //then return data from our file
        assertEquals("Connection prematurely closed BEFORE response", exception.getMessage());
    }

    @Test
    void getAllMovies_fault_randomDataThenClose() {

        //given
        stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        Exception exception = assertThrows(Exception.class, () -> movieRestClient.getAllMovies());
        //then return data from our file
        assertEquals("Connection prematurely closed BEFORE response", exception.getMessage());
    }

    @Test
    void getAllMovies_fixedDelay_timeout() {

        //given
        stubFor(get(anyUrl())
                .willReturn(ok().withFixedDelay(5500)));


        //timeOut is 5s, call making after 5s so all good, if i go above 5000 then test pass
        assertThrows(Exception.class, () -> movieRestClient.getAllMovies());

    }

    @Test
    void getAllMovies_withUniformRandomDelay_timeout() {

        //given
        stubFor(get(anyUrl())
                .willReturn(ok().withUniformRandomDelay(5500, 7000)));


        //timeOut is 5s, call making after 4s so all good, if i go above 5000 then test pass
        assertThrows(Exception.class, () -> movieRestClient.getAllMovies());

    }

    //not working
    @Test
    void deleteMovie_createMovieAndThenDelete_selective_proxying() throws MovieNotCreated, MovieNotFoundException {

        Movie movie = new Movie("chor nikal ke bhaga", 88l, "chor", LocalDate.of(1997, 2, 13), 2023);
        Movie movie1 = movieRestClient.createMovie(movie);

        String expectedResponse = "Movie Deleted Successfully";

        stubFor(delete(urlEqualTo(Get_MOVIE_BY_NAME + "?movie_name=" + movie1.getName()))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));


        String s = movieRestClient.deleteMovieByName(movie1.getName());
        System.out.println(s);
    }
}
