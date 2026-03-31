package com.reco.demo.controller;

import com.reco.demo.model.User;
import com.reco.demo.repo.UserRepository;
import com.reco.demo.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils; // Injecting our real JWT logic

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // 1. Validation
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username is required"));
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Password is required"));
            }

            // 2. Uniqueness Check
            if (userRepository.findByUsername(user.getUsername().trim()).isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Error: Username is already taken!"));
            }

            // 3. Save User
            user.setUsername(user.getUsername().trim());
            userRepository.save(user);

            // 4. Generate Token so they are logged in immediately
            String token = jwtUtils.generateToken(user.getUsername());

            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully!");
            response.put("token", token);
            response.put("username", user.getUsername());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Database Error: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody User loginRequest) {
        String username = loginRequest.getUsername() != null ? loginRequest.getUsername().trim() : null;
        String password = loginRequest.getPassword();

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing credentials"));
        }

        Optional<User> userOpt = userRepository.findByUsername(username);

        // 5. REAL JWT Login Logic
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {

            // Generate a real signed token
            String token = jwtUtils.generateToken(username);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", username);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }
    }
}