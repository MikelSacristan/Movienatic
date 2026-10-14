package com.EnSe.Movienatic.exception;

public class FollowingUserNotFoundException extends RuntimeException {
    public FollowingUserNotFoundException(Long userId, Long unfollowUserId) {
        super("El usuario con ID " + userId + " no sigue al usuario con ID " + unfollowUserId);
    }

}
