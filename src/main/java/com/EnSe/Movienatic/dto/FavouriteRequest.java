package com.EnSe.Movienatic.dto;

public class FavouriteRequest {
    private Long movieId;

    public FavouriteRequest() {
    }

    public FavouriteRequest(Long movieId) {
        this.movieId = movieId;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }
}
