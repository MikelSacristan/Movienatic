package com.EnSe.Movienatic.dto;

import java.sql.Timestamp;

// DTO para representar una reseña de película
public record ReviewDto(
                Long id,
                String content,
                Double rating,
                Integer likes,
                Integer dislikes,
                Timestamp timestamp) {

}