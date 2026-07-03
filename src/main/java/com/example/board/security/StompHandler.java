package com.example.board.security;

import com.example.board.exception.AuthException;
import com.example.board.exception.Errorcode;
import com.example.board.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String bearer = accessor.getFirstNativeHeader("Authorization");

            if (bearer != null && bearer.startsWith("Bearer ")) {
                String token = bearer.substring(7);

                jwtTokenProvider.validateAccessToken(token, "ACCESS");
                Authentication authentication = jwtTokenProvider.parseAuthentication(token);

                accessor.setUser(authentication); //principal로 등록
            } else {
                throw new AuthException(Errorcode.INVALID_TOKEN);
            }
        }

        return message;
    }
}
