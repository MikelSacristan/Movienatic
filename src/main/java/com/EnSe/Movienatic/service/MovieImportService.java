package com.EnSe.Movienatic.service;

import com.EnSe.Movienatic.dto.TmdbCastDto;
import com.EnSe.Movienatic.dto.TmdbCreditsResponse;
import com.EnSe.Movienatic.dto.TmdbMovieDto;
import com.EnSe.Movienatic.dto.TmdbResponse;
import com.EnSe.Movienatic.model.CastMember;
import com.EnSe.Movienatic.model.Movie;
import com.EnSe.Movienatic.repository.MovieRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final String imageBaseUrl;

    public MovieImportService(
            TmdbService tmdbService,
            MovieRepository movieRepository,
            @Value("${tmdb.api.image-base-url}") String imageBaseUrl) {
        this.tmdbService = tmdbService;
        this.movieRepository = movieRepository;
        this.imageBaseUrl = imageBaseUrl;
    }

    // Importa las películas populares de TMDB y las guarda en la BD si no existen
    // usando existsByTitle para evitar duplicados, devolviendo el número de
    // películas importadas, lo sacará por terminal en DataInitializer. Otra manera
    // de llamarlo sería desde un endpoint REST, por ejemplo
    // https://localhost:8080/api/movies/import, se cargaría cuando lo deseemos
    @Transactional
    public int importPopularMovies(int pages) {
        Map<Long, String> genreNames = tmdbService.fetchGenreNames(); // Mapea los IDs de género a nombres para poder
                                                                      // guardarlos en la BD
        int imported = 0;

        // Itera sobre las páginas de películas descubiertas, obteniendo los
        // resultados de TMDB y guardando en la BD si no existen
        for (int page = 1; page <= pages; page++) {
            TmdbResponse response = tmdbService.fetchDiscoveredMovies(page);

            for (TmdbMovieDto dto : response.results()) {
                if (movieRepository.existsByTitle(dto.title())) {
                    continue;
                }
                movieRepository.save(toEntity(dto, genreNames));
                imported++;
            }
        }
        return imported;
    }

    // Convierte un TmdbMovieDto a una entidad Movie, mapeando los géneros, el
    // reparto y la URL del póster, sin importar el rating
    private Movie toEntity(TmdbMovieDto dto, Map<Long, String> genreNames) {
        Movie movie = new Movie();
        movie.setTitle(dto.title());
        movie.setDescription(dto.overview() != null ? dto.overview() : "");
        movie.setGenre(mapGenres(dto.genreIds(), genreNames));
        movie.setReleaseYear(parseYear(dto.releaseDate()));
        movie.setCasting(fetchCasting(dto.id()));
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

    // Mapea los IDs de género a nombres, devolviendo "Sin género" si no hay géneros
    private String mapGenres(List<Integer> genreIds, Map<Long, String> genreNames) {
        if (genreIds == null || genreIds.isEmpty()) {
            return "Sin género";
        }
        return genreIds.stream()
                .map(id -> genreNames.getOrDefault(Long.valueOf(id), "Sin género"))
                .distinct()
                .collect(Collectors.joining(", "));
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