package com.EnSe.Movienatic.service;

import com.EnSe.Movienatic.dto.MovieDto;
import com.EnSe.Movienatic.dto.ReviewDto;
import com.EnSe.Movienatic.model.Movie;
import com.EnSe.Movienatic.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovieService {

    private MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional(readOnly = true)
    public MovieDto getMovieById(Long id) {
        return this.movieRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public MovieDto getMovieByName(String name) {
        return this.movieRepository.findByTitle(name)
                .map(this::toDto)
                .orElse(null);
    }

    private MovieDto toDto(Movie movie) {
        return new MovieDto(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getGenre(),
                movie.getReleaseYear(),
                movie.getCasting(),
                movie.getPosterURL(),
                movie.getAverageRating(),
                movie.getNumberOfRatings(),
                movie.getReviews().stream()
                        .map(review -> new ReviewDto(
                                review.getId(),
                                review.getContent(),
                                review.getRating(),
                                review.getLikes(),
                                review.getDislikes(),
                                review.getTimestamp()))
                        .toList());
    }
}