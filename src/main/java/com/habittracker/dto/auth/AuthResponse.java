package com.habittracker.dto.auth;

import com.habittracker.dto.user.UserInfoResponse;
import com.habittracker.entity.User;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String accessToken;
    String refreshToken;
    String tokenType;
    Long expiresIn;
    UserInfoResponse user;

    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(UserInfoResponse.fromUser(user))
                .build();
    }
}