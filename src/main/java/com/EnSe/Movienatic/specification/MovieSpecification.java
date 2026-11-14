package com.EnSe.Movienatic.specification;

import com.EnSe.Movienatic.dto.MovieFilter;
import com.EnSe.Movienatic.model.Movie;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MovieSpecification {

    public static Specification<Movie> withFilters(MovieFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.title() != null && !filter.title().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                "%" + filter.title().toLowerCase() + "%"));
            }

            if (filter.genreIds() != null && !filter.genreIds().isEmpty()) {
                // Unirse a la tabla intermedia movie_genres
                predicates.add(root.join("genres").get("id").in(filter.genreIds()));
            }

            if (filter.minRating() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), filter.minRating()));
            }

            if (filter.releaseYear() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("releaseYear"), filter.releaseYear()));
            }

            if (filter.releaseYearFrom() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("releaseYear"), filter.releaseYearFrom()));
            }

            if (filter.releaseYearTo() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(root.get("releaseYear"), filter.releaseYearTo()));
            }

            query.distinct(true); // necesario por el JOIN de géneros
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}