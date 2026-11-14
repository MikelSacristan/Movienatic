package com.EnSe.Movienatic.dto;

import com.fasterxml.jackson.annotation.JsonView;

public record UserDto(
        @JsonView(SummaryView.class) Long id,
        @JsonView(SummaryView.class) String username,
        @JsonView(SummaryView.class) String photoURL,
        @JsonView(DetailView.class) int favouriteCount,
        @JsonView(DetailView.class) int pendingCount,
        @JsonView(DetailView.class) int reviewCount,
        @JsonView(DetailView.class) int followingCount,
        @JsonView(DetailView.class) int followersCount
    ){
    public interface SummaryView {
    }
    
    public interface DetailView extends SummaryView {
    }

}
