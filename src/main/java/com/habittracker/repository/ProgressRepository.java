package com.habittracker.repository;

import com.habittracker.entity.Progress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    Optional<Progress> findByUserIdAndHabitIdAndDate(Long userId, Long habitId, LocalDate date);
    Page<Progress> findByUserId(Long userId, Pageable pageable);
    Page<Progress> findByHabitId(Long habitId, Pageable pageable);

    List<Progress> findByHabitIdAndDateBetween(Long habitId, LocalDate start, LocalDate end);
    List<Progress> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
    List<Progress> findByUserIdAndDate(Long userId, LocalDate date);

    List<Progress> findTop30ByHabitIdOrderByDateDesc(Long habitId);
    List<Progress> findTop30ByUserIdOrderByDateDesc(Long userId);

    boolean existsByUserIdAndHabitIdAndDate(Long userId, Long habitId, LocalDate date);
    long countByHabitIdAndDateBetween(Long habitId, LocalDate start, LocalDate end);
    long countByUserIdAndDateAfter(Long userId, LocalDate date);

    long countByUserId(Long id);
}