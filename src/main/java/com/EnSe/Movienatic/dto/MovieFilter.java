package com.EnSe.Movienatic.dto;

import java.util.List;

public record MovieFilter(String title, List<Long> genreIds, String director, Integer releaseYear, Float minRating) {
}
