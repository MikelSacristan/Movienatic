package com.EnSe.Movienatic.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;

public record UserPageResponse(
                @JsonView(UserDto.SummaryView.class) List<UserDto> content,
                @JsonView(UserDto.SummaryView.class) int totalPages,
                @JsonView(UserDto.SummaryView.class) long totalElements,
                @JsonView(UserDto.SummaryView.class) int number,
                @JsonView(UserDto.SummaryView.class) int size){}
