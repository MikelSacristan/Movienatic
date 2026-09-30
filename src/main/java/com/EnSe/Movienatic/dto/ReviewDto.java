package com.EnSe.Movienatic.dto;

public record ReviewDto(
        Long id,
        String content,
        Integer rating,
        Integer likes,
        Integer dislikes,
        Long timestamp) {

}