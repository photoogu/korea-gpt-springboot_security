package com.korit.springboot_security.service;

import com.korit.springboot_security.entity.User;
import com.korit.springboot_security.repository.UserRepository;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUserById(int userId) throws NotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자 ID는 존재하지 않습니다."));
    }

    public List<User> getAllUsers() throws NotFoundException {
        return userRepository.findAll()
                .orElseThrow(() -> new NotFoundException("사용자 정보가 존재하지 않습니다."));
    }
}

