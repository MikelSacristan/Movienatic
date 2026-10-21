package com.EnSe.Movienatic.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.stereotype.Service;

import com.EnSe.Movienatic.dto.MovieDto;
import com.EnSe.Movienatic.dto.ReviewDto;
import com.EnSe.Movienatic.dto.UpdateReviewRequest;
import com.EnSe.Movienatic.dto.UserDetailsDto;
import com.EnSe.Movienatic.dto.UserDto;
import com.EnSe.Movienatic.model.User;
import com.EnSe.Movienatic.model.Movie;
import com.EnSe.Movienatic.model.Review;
import com.EnSe.Movienatic.repository.MovieRepository;
import com.EnSe.Movienatic.repository.ReviewRepository;
import com.EnSe.Movienatic.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.EnSe.Movienatic.exception.*;

@Service
public class UserService {

    private UserRepository userRepository;
    private MovieRepository movieRepository;
    private ReviewRepository reviewRepository;

    public UserService(UserRepository userRepository, MovieRepository movieRepository,
            ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.reviewRepository = reviewRepository;
    }

    // Devuelve un bloque de usuarios paginado (por ejemplo 20 por página) ya en
    // DTO, con el total y los metadatos de paginación de la Page
    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable) {
        return this.userRepository.findAll(pageable).map(this::toUserDto);
    }

    public User getUserByName(String name) {
        return userRepository.findByUsername(name).orElseThrow(() -> new UserNotFoundException(name));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id.toString()));
    }

    // Películas pendientes
    @Transactional(readOnly = true)
    public List<MovieDto> getPendingMovies(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user.getPendingMovies()
                .stream()
                .map(this::toMovieDto)
                .toList();
    }

    @Transactional
    public void addPending(Long userId, Long movieId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));
        user.getPendingMovies().add(movie);
        userRepository.save(user);
    }

    @Transactional
    public void removePending(Long userId, Long movieId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));
        if (!user.getPendingMovies().contains(movie))
            throw new PendingMovieNotFoundException(userId, movieId);
        user.getPendingMovies().remove(movie);
        userRepository.save(user);
    }

    // Peliculas favoritas
    public List<MovieDto> getFavourites(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user.getFavouriteMovies()
                .stream()
                .map(this::toMovieDto)
                .toList();
    }

    public void addFavourite(Long userId, Long movieId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));
        user.getFavouriteMovies().add(movie);
        userRepository.save(user);
    }

    public void removeFavourite(Long userId, Long movieId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));
        if (!user.getFavouriteMovies().contains(movie))
            throw new FavoriteMovieNotFoundException(userId, movieId);
        user.getFavouriteMovies().remove(movie);
        userRepository.save(user);
    }

    // Siguiendo
    public List<UserDetailsDto> getFollowing(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user.getFollowing()
                .stream()
                .map(this::toUserDetailsDto)
                .toList();
    }

    public void followUser(Long userId, Long followUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        User userToFollow = userRepository.findById(followUserId)
                .orElseThrow(() -> new UserNotFoundException(followUserId));
        user.getFollowing().add(userToFollow);
        userRepository.save(user);
    }

    public void unfollowUser(Long userId, Long unfollowUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        User userToUnfollow = userRepository.findById(unfollowUserId)
                .orElseThrow(() -> new UserNotFoundException(unfollowUserId));
        if (!user.getFollowing().contains(userToUnfollow))
            throw new FollowingUserNotFoundException(userId, unfollowUserId);
        user.getFollowing().remove(userToUnfollow);
        userRepository.save(user);
    }

    public List<UserDetailsDto> getFollowers(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user.getFollowers()
                .stream()
                .map(this::toUserDetailsDto)
                .toList();
    }

    public List<ReviewDto> getReviews(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user.getReviews()
                .stream()
                .map(this::toReviewDto)
                .toList();
    }

    public void addReview(Long userId, Long movieId, String content, Double rating) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));
        Review review = new Review();
        review.setUser(user);
        review.setMovie(movie);
        review.setContent(content);
        review.setRating(rating);
        review.setTimestamp(new Timestamp(System.currentTimeMillis()));

        movie.getReviews().add(review);
        user.getReviews().add(review);

        recalculateMovieRating(movie);

        reviewRepository.save(review);
        userRepository.save(user);
        movieRepository.save(movie);
    }

    public ReviewDto updateReview(Long userId, Long reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId.toString()));
        Movie movie = review.getMovie();

        if (!review.getUser().getId().equals(userId)) {
            throw new ReviewNotOwnedException(userId, reviewId);
        }

        if (request.content() != null) {
            review.setContent(request.content());
        }
        if (request.rating() != null) {
            review.setRating(request.rating());
        }
        review.setTimestamp(new Timestamp(System.currentTimeMillis()));

        recalculateMovieRating(movie);

        reviewRepository.save(review);
        movieRepository.save(movie);

        return toReviewDto(review);
    }

    // Método privado para recalcular la calificación promedio de una película y el
    // número de calificaciones
    private void recalculateMovieRating(Movie movie) {
        Double averageRating = movie.getReviews().stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
        movie.setAverageRating(averageRating);
        movie.setNumberOfRatings(movie.getReviews().size());
    }

    public List<?> getLists(Long userId) {
        // TODO: Implementar la lógica para obtener las listas de un usuario
        return List.of();
    }

    private UserDetailsDto toUserDetailsDto(User user) {
        return new UserDetailsDto(user.getId(), user.getUsername());
    }

    // Método privado para convertir una entidad User a un DTO UserDto
    private UserDto toUserDto(User user) {
        return new UserDto(
                user.getUsername(),
                user.getEmail(),
                user.getPassword());
    }

    // Método privado para convertir una entidad Movie a un DTO MovieDto
    private MovieDto toMovieDto(Movie movie) {
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

    private ReviewDto toReviewDto(com.EnSe.Movienatic.model.Review review) {
        return new ReviewDto(
                review.getId(),
                review.getContent(),
                review.getRating(),
                review.getLikes(),
                review.getDislikes(),
                review.getTimestamp());
    }
}
