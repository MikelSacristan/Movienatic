package com.EnSe.Movienatic.service;

import com.EnSe.Movienatic.model.RefreshToken;
import com.EnSe.Movienatic.model.User;
import com.EnSe.Movienatic.repository.UserRepository;
import com.EnSe.Movienatic.security.AuthResponse;
import com.EnSe.Movienatic.security.JwtService;
import com.EnSe.Movienatic.security.RefreshRequest;
import com.EnSe.Movienatic.security.RefreshTokenRepository;
import com.EnSe.Movienatic.dto.LoginDto;
import com.EnSe.Movienatic.dto.RegisterDto;
import com.EnSe.Movienatic.exception.DuplicatedUserException;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder, JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
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
        // Encriptamos la contraseña antes de guardarla en la base de datos
        nuevoUsuario.setPassword(passwordEncoder.encode(registerDto.password()));
        nuevoUsuario.setEmail(registerDto.email());
        userRepository.save(nuevoUsuario);
    }

    // Busca por email y compara la contraseña. Cualquier fallo (no existe o
    // password mal) lanza la MISMA excepción, por seguridad
    public AuthResponse login(LoginDto loginDto) {
        // Autenticamos al usuario con Spring Security. Si el email o la contraseña son
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.email(),
                        loginDto.password()));

        User user = userRepository.findByEmail(loginDto.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtService.generateAccessToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail()) // subject = email
                        .password(user.getPassword())
                        .roles("USER")
                        .build());

        // Generamos un token de refresco y lo guardamos en la base de datos
        String refreshToken = UUID.randomUUID().toString();
        RefreshToken rt = new RefreshToken();
        rt.setToken(refreshToken);
        rt.setUser(user);
        rt.setExpiryDate(new Date(System.currentTimeMillis() + 604800000L)); // 7 días
        rt.setRevoked(false);
        refreshTokenRepository.save(rt);

        return new AuthResponse(token, refreshToken, user.getEmail(), user.getUsername());
    }

    public AuthResponse refreshAccessToken(RefreshRequest request) {
        RefreshToken rt = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token no válido"));

        if (rt.isRevoked() || rt.getExpiryDate().before(new Date(0))) {
            throw new RuntimeException("Refresh token expirado o revocado");
        }

        User user = rt.getUser();

        // Generar nuevo access token
        String newAccessToken = jwtService.generateAccessToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles("USER")
                        .build());

        // Rotar refresh token: invalido el viejo y creo uno nuevo
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        String newRefreshToken = UUID.randomUUID().toString();
        RefreshToken newRt = new RefreshToken();
        newRt.setToken(newRefreshToken);
        newRt.setUser(user);
        newRt.setExpiryDate(new Date(System.currentTimeMillis() + 604800000L));
        newRt.setRevoked(false);
        refreshTokenRepository.save(newRt);

        return new AuthResponse(newAccessToken, newRefreshToken, user.getEmail(), user.getUsername());
    }
}
