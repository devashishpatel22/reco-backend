package com.reco.demo.repo;

import com.reco.demo.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    // 1. Returns all recommendations, sorted by the number of unique user likes
    @Query("SELECT r FROM Recommendation r LEFT JOIN r.likedByUsers u GROUP BY r ORDER BY COUNT(u) DESC")
    List<Recommendation> findAllByOrderByLikesDesc();

    // 2. Filters by topic and sorts by the number of unique user likes
    @Query("SELECT r FROM Recommendation r LEFT JOIN r.likedByUsers u " +
            "WHERE LOWER(r.topic) LIKE LOWER(CONCAT('%', :topic, '%')) " +
            "GROUP BY r ORDER BY COUNT(u) DESC")
    List<Recommendation> findByTopicContainingIgnoreCaseOrderByLikesDesc(@Param("topic") String topic);
}
