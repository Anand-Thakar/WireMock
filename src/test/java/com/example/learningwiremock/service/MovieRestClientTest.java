package com.example.learningwiremock.service;

import com.example.learningwiremock.constants.MovieConstants;
import com.example.learningwiremock.exception.MovieNotCreated;
import com.example.learningwiremock.exception.MovieNotFoundException;
import com.example.learningwiremock.model.Movie;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static com.example.learningwiremock.constants.MovieConstants.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
//import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWireMock(port = 8090)
@TestPropertySource(properties = {"moviesapp.baseUrl=http://localhost:8090"})
public class MovieRestClientTest {
    @Autowired
    MovieRestClient movieRestClient;

//    static WireMockServer wm = new WireMockServer(options()
//            .notifier(new ConsoleNotifier(true)));



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
    void getAllMovies_specific_getUrl() throws MovieNotFoundException {

        //given
        stubFor(get(MovieConstants.GET_ALL_MOVIES)
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
    void getMovieByID_urlPathEqualsTo_workForOnly_movieID_one() throws MovieNotFoundException {

        //Given
        stubFor(get(urlPathEqualTo("/movieservice/v1/movie/1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie.json")));


        Long id = 1l;
        Movie movieById = movieRestClient.getMovieById(id);
        System.out.println(movieById);
        assertEquals(movieById.getName(), "Batman Begins");
        assertEquals(movieById.getMovie_id(), id);
    }

    @Test
    void getMovieByID_urlPathMatching_workFor_0to9_hardCoded() throws MovieNotFoundException {
        //with regex passed nine but still get the result with id 1

        //Given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie.json")));


        Long id = 9l;
        Movie movieById = movieRestClient.getMovieById(id);
        System.out.println(movieById);
        assertEquals(movieById.getName(), "Batman Begins");
    }

    @Test
    void getMovieByID_responseTemplating_dynamicResponse() throws MovieNotFoundException {

        //generate dynamic http response using response template transformer, passed movie id 500, and received movie with id 500
        //Given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie-template.json")
                        .withTransformers("response-template")));

        Long id = 500l;
        Movie movieById = movieRestClient.getMovieById(id);
        System.out.println(movieById);
        assertEquals(movieById.getName(), "Batman Begins");
        assertEquals(movieById.getMovie_id(), id);

    }

    @Test
    void getMovieByID_404NotFound() {

        //Given
        stubFor(get(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("404-movieById.json")
                        .withTransformers("response-template")));

        Long id = 500l;
        assertThrows(MovieNotFoundException.class, () -> movieRestClient.getMovieById(id));

    }

    @Test
    void getMovieByName_approach1_queryParam() throws MovieNotFoundException {

        //use this one because if you use approach 2 then without query param your test going to get passed

        String movieName = "Avengers";
        //Given
        stubFor(get(urlEqualTo(Get_MOVIE_BY_NAME + "?movie_name=" + movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("avengers.json")));

        List<Movie> avengers = movieRestClient.getMovieByName(movieName);
        System.out.println(avengers);


    }

    @Test
    void getMovieByName_approach2_queryParam_() throws MovieNotFoundException {

        String movieName = "Avengers";
        //Given
        stubFor(get(urlPathEqualTo(Get_MOVIE_BY_NAME))
//                .withQueryParam("movie_name", equalTo(movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("avengers.json")));

        List<Movie> avengers = movieRestClient.getMovieByName(movieName);
        System.out.println(avengers);
    }

    @Test
    void getMovieByName_dynamicResponse() throws MovieNotFoundException {

        //dynamic stub with name as displayed
        String movieName = "Avengersfaltu";
        //Given
        stubFor(get(urlEqualTo(Get_MOVIE_BY_NAME + "?movie_name=" + movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("dynamic-Avengers.json")
                        .withTransformers("response-template")));

        List<Movie> avengers = movieRestClient.getMovieByName(movieName);
        System.out.println(avengers);
        assertTrue(avengers.stream().allMatch(entry -> entry.getName().contains(movieName)));
    }

    @Test
    void getMovieByYear_dynamic_queryParam() throws MovieNotFoundException {

        Integer year = 2011;
        //Given
        stubFor(get(urlEqualTo(GET_MOVIE_BY_YEAR + "?year=" + year))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movieByYear.json")
                        .withTransformers("response-template")));


        List<Movie> movieByYear = movieRestClient.getMovieByYear(year);
        System.out.println(movieByYear);
        assertTrue(movieByYear.stream().allMatch(entry -> entry.getYear().equals(year)));
    }

    @Test
    void getMovieByYear_404NotFound_queryParam_dynamic_response() {

        Integer year = 2005;
        //Given
        stubFor(get(urlEqualTo(GET_MOVIE_BY_YEAR + "?year=" + year))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("404-movieByYear.json")
                        .withTransformers("response-template")));

        assertThrows(MovieNotFoundException.class, () -> movieRestClient.getMovieByYear(year));
    }

    @Test
    void createMovie_fixed_response() throws MovieNotCreated {

        Movie movie = new Movie("anand", null, "Anand's SCAM", LocalDate.of(1997, 2, 13), 1997);
        stubFor(post(CREATE_MOVIE)
                //making sure response has the following items
                .withRequestBody(matchingJsonPath("$.name", equalTo("Anand's SCAM")))
                .withRequestBody(matchingJsonPath("$.cast", containing("ana")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("createMovie.json")));


        Movie anand = movieRestClient.createMovie(movie);
        System.out.println(anand);
        assertTrue(anand.getMovie_id() != null);
    }

    @Test
    void createMovie_dynamic_response_templating() throws MovieNotCreated {

        Movie movie = new Movie("chor nikal ke bhaaga", 11L, "chor", LocalDate.of(1997, 2, 13), 1997);
        stubFor(post(CREATE_MOVIE)
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("createMovie-dynamic.json")
                        .withTransformers("response-template")));

        Movie anand = movieRestClient.createMovie(movie);
        System.out.println(anand);
        assertTrue(anand.getMovie_id() != null);
    }

    @Test
    void createMovie_badRequest_400_invalid_movieEntry() {
        Movie movie = new Movie("anand", null, null, LocalDate.of(1997, 2, 13), 1997);
        stubFor(post(CREATE_MOVIE)
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("400-invalid-input.json")
                        .withTransformers("response-template")));

        assertThrows(MovieNotCreated.class, () -> movieRestClient.createMovie(movie));
    }

    @Test
    void updateMovie_pathVariable_alongWithOtherData_dynamic() throws MovieNotCreated {

        Integer movieId = 1;
        String newCast = "Anand";
        Movie movie = new Movie(newCast, null, null, null, null);

        stubFor(put(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .withRequestBody(matchingJsonPath("$.cast", containing(newCast)))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("updateMovie.json")
                        .withTransformers("response-template")));

        Movie updatedMovie = movieRestClient.updateMovie(movieId, movie);
        System.out.println(updatedMovie);
        assertTrue(movieId == updatedMovie.getMovie_id().intValue());
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

    @Test
    void deleteMovie_notFound() {
        stubFor(delete(urlPathMatching("/movieservice/v1/movie/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
        assertThrows(MovieNotCreated.class, () -> movieRestClient.deleteMovie(765));
    }

    @Test
    void deleteMovieByName_with_verify_DSL_to_double_check() throws MovieNotCreated, MovieNotFoundException {
        stubFor(post(urlEqualTo(CREATE_MOVIE))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("createMovie-dynamic.json")
                        .withTransformers("response-template")));

        Movie movie = new Movie("chor nikal ke bhaga", 88l, "chor", LocalDate.of(1997, 2, 13), 2023);
        Movie movie1 = movieRestClient.createMovie(movie);

        String expectedResponse = "Movie Deleted Successfully";

        //don't have anybody returning void, so if I comment out all the code on the deleteMovieByName
        //still we will be facing this issue, if comment stub then also pass the test that why verify

        stubFor(delete(urlEqualTo(Get_MOVIE_BY_NAME + "?movie_name=" + movie1.getName()))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));


        String deleteResponse = movieRestClient.deleteMovieByName(movie1.getName());
        assertEquals(expectedResponse, deleteResponse);
        verify(exactly(1),postRequestedFor(urlEqualTo(CREATE_MOVIE)));
        verify(moreThan(0),deleteRequestedFor(urlEqualTo(Get_MOVIE_BY_NAME + "?movie_name=" + movie1.getName())));
    }

}