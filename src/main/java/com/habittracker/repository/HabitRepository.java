package com.habittracker.repository;

import com.habittracker.entity.Habit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {

    // Pagination et tri
    Page<Habit> findByUserId(Long userId, Pageable pageable);
    List<Habit> findByUserId(Long userId);
    Page<Habit> findByUserIdAndIsActive(Long userId, Boolean isActive, Pageable pageable);
    List<Habit> findByUserIdAndIsActive(Long userId, Boolean isActive);

    // Filtrage par catégorie et fréquence
    Page<Habit> findByUserIdAndCategory(Long userId, Habit.Category category, Pageable pageable);
    Page<Habit> findByUserIdAndFrequency(Long userId, Habit.Frequency frequency, Pageable pageable);

    // Recherche par titre
    Page<Habit> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title, Pageable pageable);

    // Habitudes avec valeur cible
    List<Habit> findByUserIdAndTargetValueIsNotNull(Long userId);

    // Comptage
    long countByUserIdAndIsActive(Long userId, Boolean isActive);
    long countByUserIdAndCategory(Long userId, Habit.Category category);
    long countByUserId(Long userId);

    long countByIsActive(boolean b);
}