package com.EnSe.Movienatic.dto;

// DTO para actualización parcial de una reseña (PATCH).
// No incluye movieId: la película se identifica por la URL de la reseña,
// no se puede cambiar mediante este endpoint.
public record UpdateReviewRequest(String content, Double rating) {}
