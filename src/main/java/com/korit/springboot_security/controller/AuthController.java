package com.korit.springboot_security.controller;

import com.korit.springboot_security.dto.request.auth.ReqSigninDto;
import com.korit.springboot_security.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody ReqSigninDto reqSigninDto) {
        return ResponseEntity.ok().body(authService.login(reqSigninDto));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup() {
        return ResponseEntity.ok().body(null);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok().body(null);
    }

}
