package com.EnSe.Movienatic.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;

public record MoviePageResponse(
        @JsonView(MovieDto.SummaryView.class) List<MovieDto> content,
        @JsonView(MovieDto.SummaryView.class) int totalPages,
        @JsonView(MovieDto.SummaryView.class) long totalElements,
        @JsonView(MovieDto.SummaryView.class) int number,
        @JsonView(MovieDto.SummaryView.class) int size
) {
}
