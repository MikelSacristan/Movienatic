package com.EnSe.Movienatic.dto;

public class ReviewRequest {
    private Long movieId;
    private String content;
    private Integer rating;

    public ReviewRequest() {
    }

    public ReviewRequest(Long movieId, String content, Integer rating) {
        this.movieId = movieId;
        this.content = content;
        this.rating = rating;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
