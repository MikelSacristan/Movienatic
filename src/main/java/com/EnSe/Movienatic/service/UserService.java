package com.EnSe.Movienatic.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.EnSe.Movienatic.dto.MovieDto;
import com.EnSe.Movienatic.dto.ReviewDto;
import com.EnSe.Movienatic.dto.UserDto;
import com.EnSe.Movienatic.model.User;
import com.EnSe.Movienatic.model.Movie;
import com.EnSe.Movienatic.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Devuelve un bloque de usuarios paginado (por ejemplo 20 por página) ya en
    // DTO, con el total y los metadatos de paginación de la Page
    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable) {
        return this.userRepository.findAll(pageable).map(this::toUserDto);
    }

    public User getUserByName(String name) {
        return userRepository.findByUsername(name).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // Peliculas pendientes
    public List<MovieDto> getPendingMovies(Long userId) {
        List<MovieDto> list = new ArrayList<>();
        for(Movie m : userRepository.findPendingMovies(userId)) list.add(toMovieDto(m));
        return list;
    }

    public void addPending(Long userId, Long movieId) {
        // TODO: Implementar la lógica para añadir una película pendiente a un usuario
    }

    public void removePending(Long userId, Long movieId) {
        // TODO: Implementar la lógica para eliminar una película pendiente de un
        // usuario
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
