package com.example.learningwiremock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Movie {
    private String cast;
    private Long movie_id;
    private String name;
    private LocalDate release_date;
    private Integer year;
}
