package com.habittracker.repository;

import com.habittracker.entity.Achievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    Page<Achievement> findByUserIdOrderByUnlockedAtDesc(Long userId, Pageable pageable);
    List<Achievement> findByUserId(Long userId);

    List<Achievement> findByUserIdAndAchievementType(Long userId, Achievement.AchievementType type);

    List<Achievement> findByUserIdAndUnlockedAtAfter(Long userId, LocalDateTime date);

    boolean existsByUserIdAndNameAndAchievementType(Long userId, String name, Achievement.AchievementType type);

    long countByUserId(Long userId);
    long countByUserIdAndAchievementType(Long userId, Achievement.AchievementType type);
}