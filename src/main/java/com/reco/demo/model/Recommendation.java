package com.reco.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;
    private String content;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "recommendation_likes",
            joinColumns = @JoinColumn(name = "recommendation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties("likedRecommendations")
    private Set<User> likedByUsers = new HashSet<>();

    // Custom Constructor ensuring Set is initialized
    public Recommendation(String topic, String content) {
        this.topic = topic;
        this.content = content;
        this.likedByUsers = new HashSet<>();
    }

    /**
     * Prevents recursion when logging or debugging.
     * We do NOT include likedByUsers in the toString.
     */
    @Override
    public String toString() {
        return "Recommendation{" +
                "id=" + id +
                ", topic='" + topic + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    @Transient
    public int getLikesCount() {
        return likedByUsers != null ? likedByUsers.size() : 0;
    }
}