package com.EnSe.Movienatic.dto;

// DTO para representar una reseña de película
public record ReviewDto(
                Long id,
                String content,
                Integer rating,
                Integer likes,
                Integer dislikes,
                Long timestamp) {

}