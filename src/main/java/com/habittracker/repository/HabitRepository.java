package com.habittracker.repository;

import com.habittracker.entity.Habit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {

    Page<Habit> findByUserId(Long userId, Pageable pageable);
    List<Habit> findByUserId(Long userId);
    List<Habit> findByUserIdAndIsActive(Long userId, Boolean isActive);

    Page<Habit> findByUserIdAndCategory(Long userId, Habit.Category category, Pageable pageable);
    Page<Habit> findByUserIdAndFrequency(Long userId, Habit.Frequency frequency, Pageable pageable);
    List<Habit> findByUserIdAndTargetValueIsNotNull(Long userId);

    Page<Habit> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title, Pageable pageable);

    long countByUserIdAndIsActive(Long userId, Boolean isActive);
    long countByUserIdAndCategory(Long userId, Habit.Category category);

    Long countByUserId(Long userId);
}