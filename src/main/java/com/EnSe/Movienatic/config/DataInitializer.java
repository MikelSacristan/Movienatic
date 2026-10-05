package com.EnSe.Movienatic.config;

import com.EnSe.Movienatic.service.MovieImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// La interfaz CommandLineRunner permite ejecutar código al iniciar la aplicación
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // Número de páginas de películas a importar desde TMDB
    private static final int PAGES_TO_IMPORT = 10;

    private final MovieImportService movieImportService; // Servicio para importar películas desde TMDB

    public DataInitializer(MovieImportService movieImportService) {
        this.movieImportService = movieImportService;
    }

    // Esto es lo que ejecuta al iniciar la aplicación: siempre consulta a TMDB
    // e importa las películas que aún no estén en la BD
    @Override
    public void run(String... args) {
        int imported = movieImportService.importPopularMovies(PAGES_TO_IMPORT);
        log.info("Importadas {} películas nuevas desde TMDB", imported);
    }
}