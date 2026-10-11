package com.EnSe.Movienatic.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.EnSe.Movienatic.dto.MovieDto;
import com.EnSe.Movienatic.dto.ReviewDto;
import com.EnSe.Movienatic.dto.UserDto;
import com.EnSe.Movienatic.model.User;
import com.EnSe.Movienatic.model.Movie;
import com.EnSe.Movienatic.repository.MovieRepository;
import com.EnSe.Movienatic.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import com.EnSe.Movienatic.exception.*;

@Service
public class UserService {

    private UserRepository userRepository;
    private MovieRepository movieRepository;

    public UserService(UserRepository userRepository, MovieRepository movieRepository ) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
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
        if(!user.getPendingMovies().contains(movie)) throw new PendingMovieNotFoundException(userId,movieId);
        user.getPendingMovies().remove(movie);
        userRepository.save(user);
    }

    // Peliculas favoritas
    public List<MovieDto> getFavourites(Long userId) {
        // TODO: Implementar la lógica para obtener las películas favoritas de un
        // usuario
        return List.of();
    }

    public void addFavourite(Long userId, Long movieId) {
        // TODO: Implementar la lógica para añadir una película favorita a un usuario
    }

    public void removeFavourite(Long userId, Long movieId) {
        // TODO: Implementar la lógica para eliminar una película favorita de un usuario
    }

    // Siguiendo
    public List<UserDto> getFollowing(Long userId) {
        // TODO: Implementar la lógica para obtener los usuarios que un usuario está
        // siguiendo
        return List.of();
    }

    public void followUser(Long userId, Long followUserId) {
        // TODO: Implementar la lógica para seguir a otro usuario
    }

    public void unfollowUser(Long userId, Long unfollowUserId) {
        // TODO: Implementar la lógica para dejar de seguir a otro usuario
    }

    public List<UserDto> getFollowers(Long userId) {
        // TODO: Implementar la lógica para obtener los seguidores de un usuario
        return List.of();
    }

    public List<?> getReviews(Long userId) {
        // TODO: Implementar la lógica para obtener las reseñas de un usuario
        return List.of();
    }

    public List<?> getLists(Long userId) {
        // TODO: Implementar la lógica para obtener las listas de un usuario
        return List.of();
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
}
