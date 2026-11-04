package com.EnSe.Movienatic.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MovieListRequest {
    private String name;
    private String description;
}
