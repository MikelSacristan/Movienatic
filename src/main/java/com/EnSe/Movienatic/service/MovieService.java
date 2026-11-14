package com.EnSe.Movienatic.service;

import com.EnSe.Movienatic.dto.GenreDto;
import com.EnSe.Movienatic.dto.MovieDto;
import com.EnSe.Movienatic.dto.MovieFilter;
import com.EnSe.Movienatic.dto.ReviewDto;
import com.EnSe.Movienatic.exception.*;
import com.EnSe.Movienatic.model.Movie;
import com.EnSe.Movienatic.repository.GenreRepository;
import com.EnSe.Movienatic.repository.MovieRepository;
import com.EnSe.Movienatic.repository.ReviewLikeRepository;
import com.EnSe.Movienatic.specification.MovieSpecification;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovieService {

    private MovieRepository movieRepository;
    private ReviewLikeRepository reviewLikeRepository;
    private GenreRepository genreRepository;

    public MovieService(MovieRepository movieRepository, ReviewLikeRepository reviewLikeRepository,
            GenreRepository genreRepository) {
        this.movieRepository = movieRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.genreRepository = genreRepository;
    }

    // Catálogo completo de géneros (usado por los filtros y la portada)
    @Transactional(readOnly = true)
    public List<GenreDto> getGenres() {
        return genreRepository.findAll().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                .sorted(Comparator.comparing(GenreDto::name))
                .toList();
    }

    // Devuelve un bloque de películas paginado (por ejemplo 20 por página) ya en
    // DTO, con el total y los metadatos de paginación de la Page
    @Transactional(readOnly = true)
    public Page<MovieDto> getMovies(MovieFilter filter, Pageable pageable) {
        Specification<Movie> spec = MovieSpecification.withFilters(filter);
        return movieRepository.findAll(spec, pageable).map(this::toMovieDto);
    }

    @Transactional(readOnly = true)
    public MovieDto getMovieById(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
        return toMovieDto(movie);
    }

    @Transactional(readOnly = true)
    public MovieDto getMovieByName(String title) {
        Movie movie = movieRepository.findByTitle(title).orElseThrow(() -> new MovieNotFoundException(title));
        return toMovieDto(movie);
    }

    // Método privado para convertir una entidad Movie a un DTO MovieDto
    private MovieDto toMovieDto(Movie movie) {
        return new MovieDto(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getGenres().stream()
                        .map(g -> new GenreDto(g.getId(), g.getName()))
                        .toList(),
                movie.getReleaseYear(),
                movie.getCasting(),
                movie.getPosterURL(),
                movie.getAverageRating(),
                movie.getNumberOfRatings(),
                movie.getReviews().stream()
                        .map(review -> new ReviewDto(
                                review.getId(),
                                review.getMovie().getId(),
                                review.getMovie().getTitle(),
                                review.getMovie().getPosterURL(),
                                review.getUser().getId(),
                                review.getUser().getUsername(),
                                review.getContent(),
                                review.getRating(),
                                reviewLikeRepository.countByReviewAndIsLike(review, true),
                                reviewLikeRepository.countByReviewAndIsLike(review, false),
                                review.getTimestamp()))
                        .toList());
    }
}