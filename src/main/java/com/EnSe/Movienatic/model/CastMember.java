package com.EnSe.Movienatic.model;

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

    @Column(name = "actor", nullable = false)
    private String name;

    @Column(name = "character_name", nullable = true)
    private String character;
}