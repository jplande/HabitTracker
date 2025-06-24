package com.habittracker.loader;

import com.habittracker.entity.*;
import com.habittracker.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(1)
@Slf4j
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    // ✅ Injection du PasswordEncoder pour hacher les mots de passe
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {

        if (userRepository.count() > 0) {
            log.info("🔄 Données existantes détectées, chargement ignoré");
            return;
        }

        log.info("🚀 Démarrage du chargement des données de test...");

        List<User> users = createUsers();
        List<Habit> habits = createHabits(users);
        createProgress(users, habits);
        createAchievements(users);

        log.info("✅ Chargement terminé avec succès !");
        logStatistics();
    }

    private List<User> createUsers() {
        log.info("👥 Création des utilisateurs avec mots de passe sécurisés...");

        List<User> users = new ArrayList<>();

        // ✅ Mots de passe hachés avec BCrypt
        String adminPassword = passwordEncoder.encode("admin123");
        String userPassword = passwordEncoder.encode("password123");

        // Admin
        User admin = new User("admin", "admin@habittracker.com", adminPassword);
        admin.setFirstName("Admin");
        admin.setLastName("System");
        admin.setRole(User.Role.ADMIN);
        users.add(admin);

        // Utilisateurs test
        User john = new User("john_doe", "john@example.com", userPassword);
        john.setFirstName("John");
        john.setLastName("Doe");
        users.add(john);

        User marie = new User("marie_martin", "marie@example.com", userPassword);
        marie.setFirstName("Marie");
        marie.setLastName("Martin");
        users.add(marie);

        User pierre = new User("pierre_durand", "pierre@example.com", userPassword);
        pierre.setFirstName("Pierre");
        pierre.setLastName("Durand");
        users.add(pierre);

        User sophie = new User("sophie_blanc", "sophie@example.com", userPassword);
        sophie.setFirstName("Sophie");
        sophie.setLastName("Blanc");
        users.add(sophie);

        List<User> savedUsers = userRepository.saveAll(users);
        log.info("✅ {} utilisateurs créés avec mots de passe BCrypt", savedUsers.size());
        log.info("🔐 Connexion: admin/admin123 ou john_doe/password123");

        return savedUsers;
    }

    /**
     * Création des habitudes d'exemple
     */
    private List<Habit> createHabits(List<User> users) {
        log.info("🎯 Création des habitudes...");

        List<Habit> habits = new ArrayList<>();

        for (User user : users) {
            if (user.getRole() == User.Role.ADMIN) continue; // Skip admin

            // Habitudes sport
            habits.add(createHabit(user.getId(), "Course matinale", "Courir 30 minutes chaque matin",
                    Habit.Category.SPORT, "minutes", Habit.Frequency.DAILY, 30.0));

            habits.add(createHabit(user.getId(), "Musculation", "Séance de musculation",
                    Habit.Category.SPORT, "séances", Habit.Frequency.WEEKLY, 3.0));

            // Habitudes santé
            habits.add(createHabit(user.getId(), "Boire de l'eau", "Boire suffisamment d'eau",
                    Habit.Category.SANTE, "litres", Habit.Frequency.DAILY, 2.0));

            habits.add(createHabit(user.getId(), "Méditation", "Méditation quotidienne",
                    Habit.Category.SANTE, "minutes", Habit.Frequency.DAILY, 10.0));

            // Habitudes éducation
            habits.add(createHabit(user.getId(), "Lecture", "Lire des livres",
                    Habit.Category.EDUCATION, "pages", Habit.Frequency.DAILY, 20.0));

            habits.add(createHabit(user.getId(), "Apprentissage langue", "Pratiquer l'anglais",
                    Habit.Category.EDUCATION, "minutes", Habit.Frequency.DAILY, 15.0));

            // Habitudes lifestyle
            habits.add(createHabit(user.getId(), "Coucher tôt", "Se coucher avant 23h",
                    Habit.Category.LIFESTYLE, "fois", Habit.Frequency.DAILY, 1.0));

            habits.add(createHabit(user.getId(), "Ranger bureau", "Maintenir un espace de travail propre",
                    Habit.Category.TRAVAIL, "fois", Habit.Frequency.WEEKLY, 2.0));
        }

        List<Habit> savedHabits = habitRepository.saveAll(habits);
        log.info("✅ {} habitudes créées", savedHabits.size());

        return savedHabits;
    }

    private Habit createHabit(Long userId, String title, String description,
                              Habit.Category category, String unit,
                              Habit.Frequency frequency, Double targetValue) {
        Habit habit = new Habit(userId, title, category, unit, frequency);
        habit.setDescription(description);
        habit.setTargetValue(targetValue);
        return habit;
    }

    /**
     * Création des progressions sur 30 jours
     */
    private void createProgress(List<User> users, List<Habit> habits) {
        log.info("📈 Création des progressions...");

        List<Progress> progressList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Habit habit : habits) {
            // Simule 30 jours de progression avec variation réaliste
            for (int dayOffset = 29; dayOffset >= 0; dayOffset--) {
                LocalDate progressDate = today.minusDays(dayOffset);

                // Probabilité de progression (80% de chance)
                if (random.nextDouble() < 0.8) {
                    Double value = generateRealisticValue(habit);
                    String note = generateRandomNote();

                    Progress progress = new Progress(habit.getUserId(), habit.getId(), progressDate, value);
                    if (random.nextDouble() < 0.3) { // 30% de chance d'avoir une note
                        progress.setNote(note);
                    }

                    progressList.add(progress);
                }
            }
        }

        List<Progress> savedProgress = progressRepository.saveAll(progressList);
        log.info("✅ {} entrées de progression créées", savedProgress.size());
    }

    /**
     * Génère une valeur réaliste selon le type d'habitude
     */
    private Double generateRealisticValue(Habit habit) {
        Double target = habit.getTargetValue();
        if (target == null) target = 1.0;

        // Variation entre 50% et 120% de l'objectif
        double variation = 0.5 + (random.nextDouble() * 0.7);
        double value = target * variation;

        // Arrondi selon l'unité
        if ("minutes".equals(habit.getUnit()) || "pages".equals(habit.getUnit())) {
            return (double) Math.round(value);
        } else if ("litres".equals(habit.getUnit())) {
            return Math.round(value * 10.0) / 10.0; // 1 décimale
        } else {
            return (double) Math.round(value);
        }
    }

    /**
     * Génère des notes aléatoires réalistes
     */
    private String generateRandomNote() {
        String[] notes = {
                "Excellent aujourd'hui !",
                "Pas facile mais j'ai tenu",
                "Très motivé ce matin",
                "Un peu fatigué mais j'ai fait l'effort",
                "Belle progression",
                "J'ai dépassé mon objectif !",
                "Bonne séance",
                "Il faut que je m'améliore",
                "Parfait timing",
                "Content de moi"
        };
        return notes[random.nextInt(notes.length)];
    }

    /**
     * Création des badges d'exemple
     */
    private void createAchievements(List<User> users) {
        log.info("🏆 Création des achievements...");

        List<Achievement> achievements = new ArrayList<>();

        for (User user : users) {
            if (user.getRole() == User.Role.ADMIN) continue;

            // Premiers badges pour tous
            achievements.add(createAchievement(user.getId(), "Bienvenue !",
                    "Premier objectif créé", "🎉", Achievement.AchievementType.MILESTONE));

            achievements.add(createAchievement(user.getId(), "Premier pas",
                    "Première progression enregistrée", "👟", Achievement.AchievementType.MILESTONE));

            // Badges de régularité (aléatoire)
            if (random.nextDouble() < 0.7) {
                achievements.add(createAchievement(user.getId(), "Régularité",
                        "7 jours consécutifs", "🔥", Achievement.AchievementType.CONSISTENCY));
            }

            if (random.nextDouble() < 0.4) {
                achievements.add(createAchievement(user.getId(), "Série impressionnante",
                        "15 jours consécutifs", "⚡", Achievement.AchievementType.STREAK));
            }

            // Badges de dépassement
            if (random.nextDouble() < 0.5) {
                achievements.add(createAchievement(user.getId(), "Surpassement",
                        "Objectif dépassé de 20%", "🚀", Achievement.AchievementType.OVERACHIEVER));
            }

            // Badges de variété
            if (random.nextDouble() < 0.6) {
                achievements.add(createAchievement(user.getId(), "Polyvalent",
                        "3 catégories différentes", "🌈", Achievement.AchievementType.VARIETY));
            }
        }

        List<Achievement> savedAchievements = achievementRepository.saveAll(achievements);
        log.info("✅ {} achievements créés", savedAchievements.size());
    }

    private Achievement createAchievement(Long userId, String name, String description,
                                          String icon, Achievement.AchievementType type) {
        Achievement achievement = new Achievement(userId, name, description, icon, type);
        // Date aléatoire dans les 30 derniers jours
        LocalDateTime unlockedAt = LocalDateTime.now().minusDays(random.nextInt(30));
        achievement.setUnlockedAt(unlockedAt);
        return achievement;
    }

    /**
     * Affiche les statistiques de chargement
     */
    private void logStatistics() {
        log.info("📊 === STATISTIQUES DES DONNÉES CHARGÉES ===");
        log.info("👥 Utilisateurs : {}", userRepository.count());
        log.info("🎯 Habitudes : {}", habitRepository.count());
        log.info("📈 Progressions : {}", progressRepository.count());
        log.info("🏆 Achievements : {}", achievementRepository.count());
        log.info("================================================");

        // Statistiques par utilisateur
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getRole() == User.Role.ADMIN) continue;

            long userHabits = habitRepository.countByUserIdAndIsActive(user.getId(), true);
            long userProgress = progressRepository.countByUserId(user.getId());
            long userAchievements = achievementRepository.countByUserId(user.getId());

            log.info("👤 {} : {} habitudes, {} progressions, {} badges",
                    user.getUsername(), userHabits, userProgress, userAchievements);
        }
    }
}