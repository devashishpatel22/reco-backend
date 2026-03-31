package com.reco.demo.controller;

import com.reco.demo.model.Recommendation;
import com.reco.demo.model.User;
import com.reco.demo.repo.RecommendationRepository;
import com.reco.demo.repo.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "http://localhost:3000")
public class RecommendationController {

    @Autowired
    private RecommendationRepository repo;

    @Autowired
    private UserRepository userRepository;

    // 1. PUBLIC: Anyone can see the ranked list
    @GetMapping
    public List<Recommendation> getRanked() {
        // We sort by likes Descending so the highest likes are at Rank #1
        return repo.findAllByOrderByLikesDesc();
    }

    // 2. SECURE: Only users with a valid JWT can post
    @PostMapping
    public ResponseEntity<?> save(@RequestBody Recommendation rec, HttpServletRequest request) {
        // Get the username verified by our JWT Filter
        String username = (String) request.getAttribute("validatedUsername");

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Session expired or invalid. Please login again."));
        }

        try {
            if (rec.getTopic() == null || rec.getTopic().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Topic is required"));
            }

            // Save the new recommendation
            Recommendation savedRec = repo.save(rec);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRec);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Backend Error: " + e.getMessage()));
        }
    }

    // 3. SECURE: Only users with a valid JWT can Like/Unlike
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeRecommendation(@PathVariable Long id, HttpServletRequest request) {
        try {
            // Get the username verified by our JWT Filter
            String username = (String) request.getAttribute("validatedUsername");

            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Login required to like posts"));
            }

            // Find the user in DB based on the JWT username
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found in system"));
            }
            User user = userOpt.get();

            Optional<Recommendation> recOpt = repo.findById(id);
            if (recOpt.isPresent()) {
                Recommendation rec = recOpt.get();

                // Toggle Like Logic
                if (rec.getLikedByUsers().contains(user)) {
                    rec.getLikedByUsers().remove(user);
                } else {
                    rec.getLikedByUsers().add(user);
                }

                Recommendation updatedRec = repo.save(rec);
                // Return updated object so React Ranking updates instantly
                return ResponseEntity.ok(updatedRec);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Post not found"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Like system error: " + e.getMessage()));
        }
    }
}