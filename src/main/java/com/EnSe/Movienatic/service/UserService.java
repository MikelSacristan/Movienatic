package com.EnSe.Movienatic.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.EnSe.Movienatic.dto.MovieDto;
import com.EnSe.Movienatic.dto.UserDto;
import com.EnSe.Movienatic.model.User;
import com.EnSe.Movienatic.repository.UserRepository;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByName(String name) {
        return userRepository.findByUsername(name).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // Peliculas pendientes
    public List<MovieDto> getPendingMovies(Long userId) {
        // TODO: Implementar la lógica para obtener las películas pendientes de un
        // usuario
        return List.of();
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
}
