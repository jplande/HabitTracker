package com.habittracker.service;

import com.habittracker.entity.*;
import com.habittracker.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Random;

@Service
@Slf4j
public class TestDataService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    private final Random random = new Random();

    /**
     * Nettoyage complet des données
     */
    @Transactional
    public void clearAllData() {
        log.info("🧹 Nettoyage de toutes les données...");

        achievementRepository.deleteAll();
        progressRepository.deleteAll();
        habitRepository.deleteAll();
        userRepository.deleteAll();

        log.info("✅ Toutes les données supprimées");
    }

    /**
     * Création d'un utilisateur de test avec données complètes
     */
    @Transactional
    public User createTestUser(String username, String email) {
        log.info("👤 Création utilisateur de test : {}", username);

        // Créer l'utilisateur
        User user = new User(username, email, "password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user = userRepository.save(user);

        // Créer des habitudes de test
        createTestHabitsForUser(user.getId());

        // Créer des progressions de test
        createTestProgressForUser(user.getId());

        // Créer des achievements de test
        createTestAchievementsForUser(user.getId());

        return user;
    }

    /**
     * Ajoute du progrès aléatoire pour aujourd'hui
     */
    @Transactional
    public void addTodayProgress(Long userId) {
        log.info("📈 Ajout de progression pour aujourd'hui - utilisateur {}", userId);

        // Récupérer les habitudes actives de l'utilisateur
        habitRepository.findByUserIdAndIsActive(userId, true).forEach(habit -> {

            // 70% de chance d'ajouter du progrès
            if (random.nextDouble() < 0.7) {

                // Vérifier qu'il n'y a pas déjà de progrès aujourd'hui
                if (!progressRepository.existsByUserIdAndHabitIdAndDate(userId, habit.getId(), LocalDate.now())) {

                    Double value = generateRealisticValue(habit);
                    Progress progress = new Progress(userId, habit.getId(), LocalDate.now(), value);

                    if (random.nextDouble() < 0.3) {
                        progress.setNote("Progression générée automatiquement");
                    }

                    progressRepository.save(progress);
                    log.info("  ✅ Progrès ajouté pour : {} ({})", habit.getTitle(), value);
                }
            }
        });
    }

    /**
     * Génère des statistiques de test
     */
    public void logTestStatistics() {
        log.info("📊 === STATISTIQUES DE TEST ===");

        userRepository.findAll().forEach(user -> {
            long habits = habitRepository.countByUserIdAndIsActive(user.getId(), true);
            long progress = progressRepository.countByUserId(user.getId());
            long achievements = achievementRepository.countByUserId(user.getId());

            log.info("👤 {} : {} habitudes, {} progressions, {} badges",
                    user.getUsername(), habits, progress, achievements);
        });
    }

    // === MÉTHODES PRIVÉES ===

    private void createTestHabitsForUser(Long userId) {
        // Sport
        Habit running = new Habit(userId, "Course", Habit.Category.SPORT, "km", Habit.Frequency.DAILY);
        running.setTargetValue(5.0);
        running.setDescription("Course quotidienne");
        habitRepository.save(running);

        // Santé
        Habit water = new Habit(userId, "Hydratation", Habit.Category.SANTE, "litres", Habit.Frequency.DAILY);
        water.setTargetValue(2.0);
        water.setDescription("Boire suffisamment d'eau");
        habitRepository.save(water);

        // Éducation
        Habit reading = new Habit(userId, "Lecture", Habit.Category.EDUCATION, "pages", Habit.Frequency.DAILY);
        reading.setTargetValue(20.0);
        reading.setDescription("Lecture quotidienne");
        habitRepository.save(reading);
    }

    private void createTestProgressForUser(Long userId) {
        habitRepository.findByUserIdAndIsActive(userId, true).forEach(habit -> {
            // 7 derniers jours de progression
            for (int i = 7; i >= 1; i--) {
                LocalDate date = LocalDate.now().minusDays(i);

                if (random.nextDouble() < 0.8) { // 80% de chance
                    Double value = generateRealisticValue(habit);
                    Progress progress = new Progress(userId, habit.getId(), date, value);
                    progressRepository.save(progress);
                }
            }
        });
    }

    private void createTestAchievementsForUser(Long userId) {
        Achievement welcome = new Achievement(userId, "Bienvenue", "Premier objectif créé",
                "🎉", Achievement.AchievementType.MILESTONE);
        achievementRepository.save(welcome);

        Achievement firstStep = new Achievement(userId, "Premier pas", "Première progression",
                "👟", Achievement.AchievementType.MILESTONE);
        achievementRepository.save(firstStep);
    }

    private Double generateRealisticValue(Habit habit) {
        Double target = habit.getTargetValue();
        if (target == null) return 1.0;

        // Variation de 50% à 120% de l'objectif
        double variation = 0.5 + (random.nextDouble() * 0.7);
        return Math.round(target * variation * 10.0) / 10.0;
    }
}