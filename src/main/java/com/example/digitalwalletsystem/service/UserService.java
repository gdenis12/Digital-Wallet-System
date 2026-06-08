package com.example.digitalwalletsystem.service;
import com.example.digitalwalletsystem.model.User;
import com.example.digitalwalletsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("A user with this email already exists!");
        }

        String securedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(securedPassword);


        user.setRole("USER");
        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    public boolean loginUser(String email, String rawPassword){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("user not found"));

        if("BLOCKED".equals(user.getStatus())){
            throw new RuntimeException("Your account has been blocked by an administrator!");
        }

        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void changePassword(Long userId, String newRawPassword){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        String encodedPassword = passwordEncoder.encode(newRawPassword);

        user.setPassword(encodedPassword);

        userRepository.save(user);
    }

    public void blockUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus("BLOCKED");
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
