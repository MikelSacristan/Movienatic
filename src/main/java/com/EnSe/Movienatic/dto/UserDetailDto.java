package com.EnSe.Movienatic.dto;

public record UserDetailDto(
        Long id,
        String username,
        String photoURL,
        int favouriteCount,
        int pendingCount,
        int reviewCount,
        int followingCount,
        int followersCount) {
}
