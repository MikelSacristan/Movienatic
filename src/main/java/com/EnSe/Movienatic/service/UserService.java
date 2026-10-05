package com.EnSe.Movienatic.service;

import com.EnSe.Movienatic.model.User;
import com.EnSe.Movienatic.repository.UserRepository;
import com.EnSe.Movienatic.exception.DuplicatedUserException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registrarUsuario(String username, String password, String email) throws DuplicatedUserException {
        var dbUser = userRepository.findByUsernameOrEmail(username, email);
        if (dbUser.isEmpty())
            throw new DuplicatedUserException(username, email);
        User nuevoUsuario = new User();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPassword(password);
        nuevoUsuario.setEmail(email);
        userRepository.save(nuevoUsuario);
    }
}
