package com.EnSe.Movienatic.dto;

public class PendingRequest {
    private Long movieId;

    public PendingRequest() {
    }

    public PendingRequest(Long userId, Long movieId) {
        this.movieId = movieId;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

}
