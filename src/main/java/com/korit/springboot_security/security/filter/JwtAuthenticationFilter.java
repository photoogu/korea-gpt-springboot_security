package com.korit.springboot_security.security.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.korit.springboot_security.entity.User;
import com.korit.springboot_security.repository.UserRepository;
import com.korit.springboot_security.security.jwt.JwtUtil;
import com.korit.springboot_security.security.principal.PrincipalUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
public class JwtAuthenticationFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String bearerToken = getAuthorization((HttpServletRequest) servletRequest);

        if(isValidToken(bearerToken)) {
            String accessToken = removeBearer(bearerToken);
            Claims claims = jwtUtil.parseToken(accessToken);
            if(claims != null) {
                int userId = Integer.parseInt(claims.getId());
                User user = getUser(userId);
                setAuthentication(user);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    // redis, mysql
    private User getUser(int userId) throws JsonProcessingException {
        User user = null;
        Object redisUser = redisTemplate.opsForValue().get("user:" + userId);
        if(redisUser != null) {
            user = objectMapper.readValue(redisUser.toString(), User.class);
        } else {
            user = userRepository.findById(userId).get();
            if(user != null) {
                String jsonUser = objectMapper.writeValueAsString(user);
                redisTemplate.opsForValue().set("user:" + userId, jsonUser, Duration.ofMinutes(10));
            }
        }
        return user;
    }

    private void setAuthentication(User user) {
        if(user == null) {
            return;
        }
        PrincipalUser principalUser = new PrincipalUser(user);
        Authentication authentication
                = new UsernamePasswordAuthenticationToken(
                principalUser,
                null,
                principalUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getAuthorization(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    private boolean isValidToken(String token) {
        return token != null && token.startsWith("Bearer ");
    }

    private String removeBearer(String bearerToken) {
        return bearerToken.substring(7);
    }
}
