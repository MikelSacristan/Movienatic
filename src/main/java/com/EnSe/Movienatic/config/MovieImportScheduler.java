package com.EnSe.Movienatic.config;

import com.EnSe.Movienatic.service.MovieImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Importación periódica de películas nuevas desde TMDB.
// Por defecto se ejecuta todos los domingos a las 12:00 (mediodía).
@Component
public class MovieImportScheduler {

    private static final Logger log = LoggerFactory.getLogger(MovieImportScheduler.class);

    private final MovieImportService movieImportService;

    // Número de páginas de películas a importar desde TMDB en cada ejecución
    @Value("${movies.import.pages:50}")
    private int pagesToImport;

    public MovieImportScheduler(MovieImportService movieImportService) {
        this.movieImportService = movieImportService;
    }

    // "0 0 12 * * SUN" = cada domingo a las 12:00.
    @Scheduled(cron = "${movies.import.cron:0 0 12 * * SUN}", zone = "${movies.import.zone:Europe/Madrid}")
    public void importNewMovies() {
        log.info("Iniciando importación periódica de películas desde TMDB ({} páginas)", pagesToImport);
        try {
            int imported = movieImportService.importPopularMovies(pagesToImport);
            log.info("Importación periódica finalizada: {} películas nuevas desde TMDB", imported);
        } catch (RuntimeException e) {
            // No relanzamos: así el scheduler sigue vivo y lo intentará de nuevo
            // en la siguiente ejecución.
            log.error("Falló la importación periódica de películas desde TMDB", e);
        }
    }
}
