package com.EnSe.Movienatic.dto;

import java.util.List;

public record MovieFilter(String title, List<Long> genreIds, Integer releaseYear,
                Float minRating, Integer releaseYearFrom, Integer releaseYearTo) {
}
