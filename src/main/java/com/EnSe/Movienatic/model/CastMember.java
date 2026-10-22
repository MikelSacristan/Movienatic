package com.EnSe.Movienatic.model;

import com.EnSe.Movienatic.dto.MovieDto;
import com.fasterxml.jackson.annotation.JsonView;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CastMember {

    @JsonView({MovieDto.DetailView.class, MovieDto.CastingView.class})
    @Column(name = "actor", nullable = false)
    private String name;

    @JsonView({MovieDto.DetailView.class, MovieDto.CastingView.class})
    @Column(name = "character_name", nullable = true)
    private String character;
}