package com.EnSe.Movienatic.service;

import com.EnSe.Movienatic.model.User;
import com.EnSe.Movienatic.repository.UserRepository;
import com.EnSe.Movienatic.dto.LoginDto;
import com.EnSe.Movienatic.dto.RegisterDto;
import com.EnSe.Movienatic.exception.BadCredentialsException;
import com.EnSe.Movienatic.exception.DuplicatedUserException;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Registra un nuevo usuario en la base de datos. Antes de guardar, comprueba si
    // ya existe un usuario con el mismo nombre de usuario o correo electrónico. Si
    // existe, lanza una excepción DuplicatedUserException.
    public void registrarUsuario(RegisterDto registerDto) {
        List<User> usuariosExistentes = userRepository.findByUsernameOrEmail(registerDto.username(),
                registerDto.email());
        if (!usuariosExistentes.isEmpty())
            throw new DuplicatedUserException(registerDto.username(), registerDto.email());

        User nuevoUsuario = new User();
        nuevoUsuario.setUsername(registerDto.username());
        nuevoUsuario.setPassword(registerDto.password());
        nuevoUsuario.setEmail(registerDto.email());
        userRepository.save(nuevoUsuario);
    }

    // Busca por email y compara la contraseña. Cualquier fallo (no existe o
    // password mal) lanza la MISMA excepción, por seguridad
    public void login(LoginDto loginDto) {
        User usuario = userRepository.findByEmail(loginDto.email())
                .orElseThrow(BadCredentialsException::new);

        if (!usuario.getPassword().equals(loginDto.password()))
            throw new BadCredentialsException();
    }
}
