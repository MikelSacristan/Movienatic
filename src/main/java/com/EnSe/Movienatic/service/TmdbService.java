package com.EnSe.Movienatic.service;

import com.EnSe.Movienatic.dto.TmdbCreditsResponse;
import com.EnSe.Movienatic.dto.TmdbGenreDto;
import com.EnSe.Movienatic.dto.TmdbGenreResponse;
import com.EnSe.Movienatic.dto.TmdbResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

// Servicio inicial para interactuar con la API de TMDB
@Service
public class TmdbService {

    private final RestClient restClient;

    public TmdbService(
            // Recibe 2 Value de application.properties para configurar la URL base y el
            // token de autenticación (read API key)
            @Value("${tmdb.api.base-url}") String baseUrl,
            @Value("${tmdb.api.token}") String token) {

        // Configura el RestClient con la URL base y el token de autenticación
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    // Trae películas traducidas al español del catálogo general de TMDB
    public TmdbResponse fetchDiscoveredMovies(int page) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/discover/movie")
                        .queryParam("language", "es-ES")
                        .queryParam("sort_by", "popularity.desc")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(TmdbResponse.class);
    }

    // Trae los géneros de película para mapear los genre_ids a nombres, ya que
    // tenemos TMDB trae los géneros con IDs
    public Map<Long, String> fetchGenreNames() {
        TmdbGenreResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/genre/movie/list")
                        .queryParam("language", "es-ES")
                        .build())
                .retrieve()
                .body(TmdbGenreResponse.class);

        Map<Long, String> genreNames = new LinkedHashMap<>();
        if (response != null && response.genres() != null) {
            for (TmdbGenreDto genre : response.genres()) {
                genreNames.put(genre.id(), genre.name());
            }
        }
        return genreNames;
    }

    // Trae el reparto (casting) de una película por su id de TMDB
    public TmdbCreditsResponse fetchCredits(Long movieId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{movieId}/credits")
                        .queryParam("language", "es-ES")
                        .build(movieId))
                .retrieve()
                .body(TmdbCreditsResponse.class);
    }
}