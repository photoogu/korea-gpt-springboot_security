package com.korit.springboot_security.service;

import com.korit.springboot_security.dto.request.auth.ReqSigninDto;
import com.korit.springboot_security.dto.response.RespAuthDto;
import com.korit.springboot_security.entity.User;
import com.korit.springboot_security.repository.UserRepository;
import com.korit.springboot_security.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTokenService redisTokenService;

    public RespAuthDto login(ReqSigninDto reqSigninDto) {
        User foundUser = userRepository.findByUsername(reqSigninDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("사용자 정보를 확인하세요."));

        if (!bCryptPasswordEncoder.matches(reqSigninDto.getPassword(), foundUser.getPassword())) {
            throw new BadCredentialsException("사용자 정보를 확인하세요.");
        }

        String accessToken = jwtUtil
                .generateToken(Integer.toString(
                        foundUser.getUserId()),
                        foundUser.getUsername(),
                        false);
        String refreshToken = jwtUtil
                .generateToken(Integer.toString(
                        foundUser.getUserId()),
                        foundUser.getUsername(),
                        true);

        redisTokenService.setAccess(reqSigninDto.getUsername(), accessToken, Duration.ofMinutes(60));
        redisTokenService.setRefresh(reqSigninDto.getUsername(), refreshToken, Duration.ofDays(7));

        return RespAuthDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
