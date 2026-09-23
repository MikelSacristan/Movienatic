package com.EnSe.Movienatic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CastMember {

    @Column(name = "actor", nullable = false)
    private String name;

    @Column(name = "character_name", nullable = true)
    private String character;

    public CastMember() {
    }

    public CastMember(String name, String character) {
        this.name = name;
        this.character = character;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }
}