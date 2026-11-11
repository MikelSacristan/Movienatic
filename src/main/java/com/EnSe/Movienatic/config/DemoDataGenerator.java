package com.EnSe.Movienatic.config;

import com.EnSe.Movienatic.model.Movie;
import com.EnSe.Movienatic.model.User;
import com.EnSe.Movienatic.repository.MovieRepository;
import com.EnSe.Movienatic.repository.UserRepository;
import com.EnSe.Movienatic.service.MovieService;
import com.EnSe.Movienatic.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@Profile("demo") // solo se ejecuta si activas el perfil "demo"
public class DemoDataGenerator implements CommandLineRunner {

    private static final int USERS_TO_CREATE = 40;
    private static final int REVIEWS_PER_USER = 4;

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final UserService userService;
    private final Random random = new Random();

    private static final String[] CONTENTS = {
            "Me ha gustado mucho esta película.",
            "No me ha convencido el final.",
            "Actuaciones sobresalientes.",
            "Guion predecible pero entretenido.",
            "Una obra maestra del cine.",
            "No la recomendaría.",
            "Mejor de lo que esperaba.",
            "Los efectos especiales están genial."
    };

    public DemoDataGenerator(UserRepository userRepository,
            MovieRepository movieRepository,
            UserService userService,
            MovieService movieService) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        // Evita duplicar si ya hay datos
        if (userRepository.count() > 0) {
            System.out.println("Ya hay usuarios en la base de datos. Se omite la generación de datos demo.");
            return;
        }

        List<Movie> movies = movieRepository.findAll();
        if (movies.isEmpty()) {
            System.out.println("No hay películas importadas. Importa películas primero con el perfil por defecto.");
            return;
        }

        // Crear usuarios
        for (int i = 1; i <= USERS_TO_CREATE; i++) {
            User user = new User();
            user.setUsername("usuario_demo_" + i);
            user.setEmail("usuario_demo_" + i + "@example.com");
            user.setPassword("password123");
            user = userRepository.save(user);

            // Crear reviews aleatorias
            for (int j = 0; j < REVIEWS_PER_USER; j++) {
                Movie movie = movies.get(random.nextInt(movies.size()));
                double rating = 0.5 + (random.nextInt(45) * 0.1); // entre 0.5 y 5.0
                String content = CONTENTS[random.nextInt(CONTENTS.length)];
                try {
                    userService.addReview(user.getId(), movie.getId(), content, rating);
                } catch (Exception ignored) {
                    // Si ya existe review de ese usuario para esa película, se ignora
                }
            }

            // Añadir algunas películas a favoritos
            for (int j = 0; j < random.nextInt(5); j++) {
                Movie movie = movies.get(random.nextInt(movies.size()));
                try {
                    userService.addFavourite(user.getId(), movie.getId());
                } catch (Exception ignored) {
                }
            }

            // Añadir algunas películas a pendientes
            for (int j = 0; j < random.nextInt(3); j++) {
                Movie movie = movies.get(random.nextInt(movies.size()));
                try {
                    userService.addPending(user.getId(), movie.getId());
                } catch (Exception ignored) {
                }
            }
        }

        // Crear follows aleatorios entre usuarios
        List<User> users = userRepository.findAll();
        for (User follower : users) {
            int follows = random.nextInt(5);
            for (int j = 0; j < follows; j++) {
                User following = users.get(random.nextInt(users.size()));
                if (!following.getId().equals(follower.getId())) {
                    try {
                        userService.followUser(follower.getId(), following.getId());
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        System.out.println("Datos de demostración creados: " + users.size() +
                " usuarios con reviews, favoritos, pendientes y follows.");
    }
}