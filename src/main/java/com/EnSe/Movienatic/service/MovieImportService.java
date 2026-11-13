package com.EnSe.Movienatic.service;

import com.EnSe.Movienatic.dto.TmdbCreditsResponse;
import com.EnSe.Movienatic.dto.TmdbMovieDto;
import com.EnSe.Movienatic.dto.TmdbResponse;
import com.EnSe.Movienatic.model.CastMember;
import com.EnSe.Movienatic.model.Genre;
import com.EnSe.Movienatic.model.Movie;
import com.EnSe.Movienatic.repository.GenreRepository;
import com.EnSe.Movienatic.repository.MovieRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovieImportService {

    private static final Logger log = LoggerFactory.getLogger(MovieImportService.class);

    private static final int MAX_CAST_SIZE = 10;

    private final TmdbService tmdbService;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final String imageBaseUrl;

    public MovieImportService(
            TmdbService tmdbService,
            MovieRepository movieRepository,
            GenreRepository genreRepository,
            @Value("${tmdb.api.image-base-url}") String imageBaseUrl) {
        this.tmdbService = tmdbService;
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.imageBaseUrl = imageBaseUrl;
    }

    // Importa las películas descubiertas de TMDB y guarda en la BD solo las que no
    // están ya importadas. Los títulos existentes se obtienen en una única
    // consulta y se comparan en memoria. Devuelve el número de películas nuevas
    // importadas.
    @Transactional
    public int importPopularMovies(int pages) {
        Map<Long, String> genreNames = tmdbService.fetchGenreNames(); // Mapea los IDs de TMDB a nombres de género
        Map<Long, Genre> genreEntities = ensureGenresExist(genreNames); // Busca o crea entidades Genre en la BD

        // En cada inicio de la aplicación, se obtienen los títulos existentes en la BD
        // para evitar duplicados
        Set<String> existingTitles = new HashSet<>(movieRepository.findAllTitles());
        List<Movie> newMovies = new ArrayList<>();
        int imported = 0;

        // Itera sobre las páginas de películas descubiertas, obteniendo los
        // resultados de TMDB y guardando en la BD las que no existen todavía.
        // Se añaden pausas para respetar el rate limit de TMDB (40 req / 10 s).
        for (int page = 1; page <= pages; page++) {
            TmdbResponse response = tmdbService.fetchDiscoveredMovies(page);

            for (TmdbMovieDto dto : response.results()) {
                if (!existingTitles.add(dto.title())) {
                    continue;
                }
                newMovies.add(toEntity(dto, genreEntities));
                imported++;

                // Pausa corta entre películas para no saturar el casting de TMDB
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // Pausa entre páginas
            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        movieRepository.saveAll(newMovies);
        return imported;
    }

    // Busca en BD cada género por nombre; si no existe, lo crea. Devuelve un mapa
    // de id de TMDB -> entidad Genre para poder relacionar las películas.
    private Map<Long, Genre> ensureGenresExist(Map<Long, String> genreNames) {
        Map<Long, Genre> genreEntities = new HashMap<>();
        if (genreNames == null) {
            return genreEntities;
        }
        for (Map.Entry<Long, String> entry : genreNames.entrySet()) {
            Genre genre = genreRepository.findByName(entry.getValue())
                    .orElseGet(() -> genreRepository.save(new Genre(entry.getValue())));
            genreEntities.put(entry.getKey(), genre);
        }
        return genreEntities;
    }

    // Convierte un TmdbMovieDto a una entidad Movie, mapeando los géneros, el
    // reparto y la URL del póster, sin importar el rating
    private Movie toEntity(TmdbMovieDto dto, Map<Long, Genre> genreEntities) {
        Movie movie = new Movie();
        movie.setTitle(dto.title());
        movie.setDescription(dto.overview() != null ? dto.overview() : "");
        movie.setGenres(mapGenres(dto.genreIds(), genreEntities));
        movie.setReleaseYear(parseYear(dto.releaseDate()));
        movie.setCasting(fetchCasting(dto.id())); // Obtiene el reparto de la película desde TMDB, limitando a
                                                  // MAX_CAST_SIZE y ordenando por relevancia
        movie.setPosterURL(dto.posterPath() != null ? imageBaseUrl + dto.posterPath() : null);
        return movie;
    }

    // Llama a /movie/{id}/credits y devuelve los 10 primeros del reparto
    // (actor y personaje) ordenados por relevancia; si falla, deja vacío
    private List<CastMember> fetchCasting(Long movieId) {
        try {
            TmdbCreditsResponse credits = tmdbService.fetchCredits(movieId);
            if (credits == null || credits.cast() == null) {
                return List.of();
            }
            return credits.cast().stream()
                    .filter(c -> c.name() != null)
                    .sorted(Comparator.comparing(c -> c.order() == null ? Integer.MAX_VALUE : c.order()))
                    .limit(MAX_CAST_SIZE)
                    .map(c -> new CastMember(c.name(), c.character()))
                    .toList();
        } catch (RuntimeException e) {
            log.warn("No se pudo obtener el casting de la película TMDB {}", movieId, e);
            return List.of();
        }
    }

    // Mapea los IDs de género de TMDB a entidades Genre existentes en BD
    private Set<Genre> mapGenres(List<Integer> genreIds, Map<Long, Genre> genreEntities) {
        Set<Genre> genres = new HashSet<>();
        if (genreIds == null || genreIds.isEmpty() || genreEntities == null) {
            return genres;
        }
        for (Integer id : genreIds) {
            Genre genre = genreEntities.get(Long.valueOf(id));
            if (genre != null) {
                genres.add(genre);
            }
        }
        return genres;
    }

    // Extrae el año de la fecha de lanzamiento, devolviendo 0 si no es válido
    private int parseYear(String releaseDate) {
        if (releaseDate == null || releaseDate.isBlank() || releaseDate.length() < 4) {
            return 0;
        }
        try {
            return Integer.parseInt(releaseDate.substring(0, 4));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
