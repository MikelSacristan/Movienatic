package com.EnSe.Movienatic.config;

import com.EnSe.Movienatic.repository.MovieRepository;
import com.EnSe.Movienatic.service.MovieImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// La interfaz CommandLineRunner permite ejecutar código al iniciar la aplicación
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // Número de páginas de películas populares a importar desde TMDB
    private static final int PAGES_TO_IMPORT = 10;

    private final MovieImportService movieImportService; // Servicio para importar películas desde TMDB
    private final MovieRepository movieRepository; // Repositorio para acceder a la base de datos de películas

    public DataInitializer(MovieImportService movieImportService, MovieRepository movieRepository) {
        this.movieImportService = movieImportService;
        this.movieRepository = movieRepository;
    }

    // Esto es lo que ejecutará al iniciar la aplicación, importando películas si la
    // base de datos está vacía
    @Override
    public void run(String... args) {
        if (movieRepository.count() > 0) {
            log.info("Ya hay películas en la BD, no se importa de nuevo");
            return;
        }

        int imported = movieImportService.importPopularMovies(PAGES_TO_IMPORT);
        log.info("Importadas {} películas desde TMDB", imported);
    }
}