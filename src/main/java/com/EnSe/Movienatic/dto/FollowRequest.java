package com.EnSe.Movienatic.dto;

public class FollowRequest {
    private Long userIdToFollow;

    public FollowRequest() {
    }

    public FollowRequest(Long userIdToFollow) {
        this.userIdToFollow = userIdToFollow;
    }

    public Long getUserIdToFollow() {
        return userIdToFollow;
    }

    public void setUserIdToFollow(Long userIdToFollow) {
        this.userIdToFollow = userIdToFollow;
    }
}
