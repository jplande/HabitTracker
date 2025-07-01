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

    @Autowired private UserRepository userRepository;
    @Autowired private HabitRepository habitRepository;
    @Autowired private ProgressRepository progressRepository;
    @Autowired private AchievementRepository achievementRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final Random random = new Random(42); // Seed fixe pour reproductibilité

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("🔄 Données existantes détectées, chargement ignoré");
            return;
        }

        log.info("🚀 === CRÉATION JEU DE DONNÉES COMPLET ===");

        List<User> users = createDiverseUsers();
        List<Habit> habits = createVariedHabits(users);
        createRealisticProgress(users, habits);
        createMeaningfulAchievements(users);

        logDetailedStatistics();
        log.info("✅ === CHARGEMENT TERMINÉ AVEC SUCCÈS ===");
    }

    // =====================================================================
    // ÉTAPE 1 : CRÉATION D'UTILISATEURS DIVERSIFIÉS
    // =====================================================================

    private List<User> createDiverseUsers() {
        log.info("👥 Création d'utilisateurs diversifiés...");

        List<User> users = new ArrayList<>();
        String password = passwordEncoder.encode("password123");

        // 1. Admin principal
        users.add(createUser("admin", "admin@habittracker.com",
                passwordEncoder.encode("admin123"), "Admin", "System",
                User.Role.ADMIN, true, 180)); // 6 mois

        // 2. Utilisateurs actifs avec différentes anciennetés
        users.add(createUser("marie_champion", "marie@example.com", password,
                "Marie", "Champion", User.Role.USER, true, 150)); // 5 mois - très active

        users.add(createUser("paul_regulier", "paul@example.com", password,
                "Paul", "Régulier", User.Role.USER, true, 90)); // 3 mois - régulier

        users.add(createUser("alice_novice", "alice@example.com", password,
                "Alice", "Novice", User.Role.USER, true, 30)); // 1 mois - débutante

        users.add(createUser("tom_sportif", "tom@example.com", password,
                "Tom", "Sportif", User.Role.USER, true, 120)); // 4 mois - focus sport

        users.add(createUser("emma_studieuse", "emma@example.com", password,
                "Emma", "Studieuse", User.Role.USER, true, 75)); // 2.5 mois - focus éducation

        // 3. Utilisateurs moins actifs
        users.add(createUser("jules_intermittent", "jules@example.com", password,
                "Jules", "Intermittent", User.Role.USER, true, 60)); // 2 mois - irrégulier

        // 4. Utilisateurs inactifs (max 4 comme demandé)
        users.add(createUser("sarah_inactive", "sarah@example.com", password,
                "Sarah", "Inactive", User.Role.USER, false, 200)); // Ancienne, désactivée

        users.add(createUser("alex_abandonne", "alex@example.com", password,
                "Alex", "Abandonné", User.Role.USER, false, 100)); // A abandonné récemment

        users.add(createUser("lucas_suspendu", "lucas@example.com", password,
                "Lucas", "Suspendu", User.Role.USER, false, 45)); // Suspendu temporairement

        users.add(createUser("lisa_teste", "lisa@example.com", password,
                "Lisa", "Test", User.Role.USER, false, 10)); // Compte de test inactif

        List<User> savedUsers = userRepository.saveAll(users);

        log.info("✅ {} utilisateurs créés :", savedUsers.size());
        savedUsers.forEach(user -> log.info("   👤 {} ({}) - {} - Inscrit il y a {} jours",
                user.getUsername(),
                user.getRole(),
                user.getIsActive() ? "Actif" : "Inactif",
                java.time.temporal.ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), LocalDate.now())
        ));

        return savedUsers;
    }

    private User createUser(String username, String email, String password,
                            String firstName, String lastName, User.Role role,
                            boolean isActive, int daysAgo) {

        User user = new User(username, email, password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setIsActive(isActive);

        // Définir date de création personnalisée
        LocalDateTime createdAt = LocalDateTime.now().minusDays(daysAgo);
        user.setCreatedAt(createdAt);

        return user;
    }

    // =====================================================================
    // ÉTAPE 2 : CRÉATION D'HABITUDES VARIÉES PAR PROFIL
    // =====================================================================

    private List<Habit> createVariedHabits(List<User> users) {
        log.info("🎯 Création d'habitudes variées par profil...");

        List<Habit> allHabits = new ArrayList<>();

        for (User user : users) {
            if (user.getRole() == User.Role.ADMIN) continue; // Skip admin

            List<Habit> userHabits = switch (user.getUsername()) {
                case "marie_champion" -> createChampionHabits(user.getId());
                case "paul_regulier" -> createRegularHabits(user.getId());
                case "alice_novice" -> createNoviceHabits(user.getId());
                case "tom_sportif" -> createSportHabits(user.getId());
                case "emma_studieuse" -> createStudyHabits(user.getId());
                case "jules_intermittent" -> createIntermittentHabits(user.getId());
                default -> createInactiveHabits(user.getId()); // Pour les comptes inactifs
            };

            allHabits.addAll(userHabits);
        }

        List<Habit> savedHabits = habitRepository.saveAll(allHabits);

        log.info("✅ {} habitudes créées :", savedHabits.size());
        users.forEach(user -> {
            if (user.getRole() != User.Role.ADMIN) {
                long userHabitCount = savedHabits.stream()
                        .filter(h -> h.getUserId().equals(user.getId()))
                        .count();
                log.info("   🎯 {} : {} habitudes", user.getUsername(), userHabitCount);
            }
        });

        return savedHabits;
    }

    // === PROFILS D'HABITUDES ===

    private List<Habit> createChampionHabits(Long userId) {
        return List.of(
                // Sport (très active)
                createHabit(userId, "Course matinale", "Running 5km chaque matin",
                        Habit.Category.SPORT, "km", Habit.Frequency.DAILY, 5.0),
                createHabit(userId, "Musculation", "Séances de renforcement",
                        Habit.Category.SPORT, "séances", Habit.Frequency.WEEKLY, 4.0),
                createHabit(userId, "Yoga", "Séance de yoga relaxante",
                        Habit.Category.SANTE, "minutes", Habit.Frequency.DAILY, 30.0),

                // Santé & Bien-être
                createHabit(userId, "Méditation", "Pleine conscience quotidienne",
                        Habit.Category.SANTE, "minutes", Habit.Frequency.DAILY, 15.0),
                createHabit(userId, "Hydratation", "Consommation d'eau optimale",
                        Habit.Category.SANTE, "litres", Habit.Frequency.DAILY, 2.5),

                // Développement personnel
                createHabit(userId, "Lecture", "Lecture de développement personnel",
                        Habit.Category.EDUCATION, "pages", Habit.Frequency.DAILY, 25.0),
                createHabit(userId, "Journaling", "Écriture quotidienne",
                        Habit.Category.LIFESTYLE, "pages", Habit.Frequency.DAILY, 2.0),

                // Productivité
                createHabit(userId, "Révision objectifs", "Point hebdomadaire",
                        Habit.Category.TRAVAIL, "sessions", Habit.Frequency.WEEKLY, 1.0)
        );
    }

    private List<Habit> createRegularHabits(Long userId) {
        return List.of(
                createHabit(userId, "Marche quotidienne", "30 minutes de marche",
                        Habit.Category.SPORT, "minutes", Habit.Frequency.DAILY, 30.0),
                createHabit(userId, "Lecture", "Lecture avant coucher",
                        Habit.Category.EDUCATION, "pages", Habit.Frequency.DAILY, 15.0),
                createHabit(userId, "Eau", "Hydratation régulière",
                        Habit.Category.SANTE, "verres", Habit.Frequency.DAILY, 8.0),
                createHabit(userId, "Coucher tôt", "Se coucher avant 23h",
                        Habit.Category.LIFESTYLE, "fois", Habit.Frequency.DAILY, 1.0),
                createHabit(userId, "Rangement bureau", "Workspace propre",
                        Habit.Category.TRAVAIL, "fois", Habit.Frequency.WEEKLY, 2.0)
        );
    }

    private List<Habit> createNoviceHabits(Long userId) {
        return List.of(
                createHabit(userId, "Marche", "Premiers pas vers le sport",
                        Habit.Category.SPORT, "minutes", Habit.Frequency.DAILY, 20.0),
                createHabit(userId, "Boire plus d'eau", "Améliorer hydratation",
                        Habit.Category.SANTE, "verres", Habit.Frequency.DAILY, 6.0),
                createHabit(userId, "Lire 10 min", "Reprendre la lecture",
                        Habit.Category.EDUCATION, "minutes", Habit.Frequency.DAILY, 10.0)
        );
    }

    private List<Habit> createSportHabits(Long userId) {
        return List.of(
                createHabit(userId, "Running", "Course d'endurance",
                        Habit.Category.SPORT, "km", Habit.Frequency.DAILY, 7.0),
                createHabit(userId, "Musculation", "Programme complet",
                        Habit.Category.SPORT, "séances", Habit.Frequency.WEEKLY, 5.0),
                createHabit(userId, "Natation", "Entraînement aquatique",
                        Habit.Category.SPORT, "longueurs", Habit.Frequency.WEEKLY, 50.0),
                createHabit(userId, "Étirements", "Récupération active",
                        Habit.Category.SANTE, "minutes", Habit.Frequency.DAILY, 20.0),
                createHabit(userId, "Protéines", "Nutrition sportive",
                        Habit.Category.SANTE, "portions", Habit.Frequency.DAILY, 3.0),
                createHabit(userId, "Sommeil", "Récupération optimale",
                        Habit.Category.LIFESTYLE, "heures", Habit.Frequency.DAILY, 8.0)
        );
    }

    private List<Habit> createStudyHabits(Long userId) {
        return List.of(
                createHabit(userId, "Étude langues", "Apprentissage anglais",
                        Habit.Category.EDUCATION, "minutes", Habit.Frequency.DAILY, 45.0),
                createHabit(userId, "Lecture technique", "Veille technologique",
                        Habit.Category.EDUCATION, "articles", Habit.Frequency.DAILY, 3.0),
                createHabit(userId, "Code practice", "Exercices de programmation",
                        Habit.Category.EDUCATION, "exercices", Habit.Frequency.DAILY, 5.0),
                createHabit(userId, "Podcasts", "Écoute éducative",
                        Habit.Category.EDUCATION, "épisodes", Habit.Frequency.DAILY, 1.0),
                createHabit(userId, "Notes", "Synthèse apprentissages",
                        Habit.Category.TRAVAIL, "pages", Habit.Frequency.DAILY, 2.0)
        );
    }

    private List<Habit> createIntermittentHabits(Long userId) {
        return List.of(
                createHabit(userId, "Sport parfois", "Activité sporadique",
                        Habit.Category.SPORT, "sessions", Habit.Frequency.WEEKLY, 2.0),
                createHabit(userId, "Lecture", "Lecture irrégulière",
                        Habit.Category.EDUCATION, "pages", Habit.Frequency.DAILY, 10.0),
                createHabit(userId, "Méditation", "Tentatives de méditation",
                        Habit.Category.SANTE, "minutes", Habit.Frequency.DAILY, 5.0)
        );
    }

    private List<Habit> createInactiveHabits(Long userId) {
        return List.of(
                createHabitInactive(userId, "Objectif abandonné", "Habitude non suivie",
                        Habit.Category.SPORT, "fois", Habit.Frequency.DAILY, 1.0),
                createHabitInactive(userId, "Projet suspendu", "En pause",
                        Habit.Category.EDUCATION, "heures", Habit.Frequency.WEEKLY, 5.0)
        );
    }

    private Habit createHabit(Long userId, String title, String description,
                              Habit.Category category, String unit,
                              Habit.Frequency frequency, Double targetValue) {
        Habit habit = new Habit(userId, title, category, unit, frequency);
        habit.setDescription(description);
        habit.setTargetValue(targetValue);
        habit.setIsActive(true);
        return habit;
    }

    private Habit createHabitInactive(Long userId, String title, String description,
                                      Habit.Category category, String unit,
                                      Habit.Frequency frequency, Double targetValue) {
        Habit habit = createHabit(userId, title, description, category, unit, frequency, targetValue);
        habit.setIsActive(false);
        return habit;
    }

    // =====================================================================
    // ÉTAPE 3 : CRÉATION DE PROGRESSIONS RÉALISTES
    // =====================================================================

    private void createRealisticProgress(List<User> users, List<Habit> habits) {
        log.info("📈 Création de progressions réalistes...");

        List<Progress> allProgress = new ArrayList<>();

        for (User user : users) {
            if (user.getRole() == User.Role.ADMIN || !user.getIsActive()) {
                continue; // Skip admin et inactifs
            }

            List<Habit> userHabits = habits.stream()
                    .filter(h -> h.getUserId().equals(user.getId()) && h.getIsActive())
                    .toList();

            List<Progress> userProgress = switch (user.getUsername()) {
                case "marie_champion" -> createChampionProgress(user, userHabits);
                case "paul_regulier" -> createRegularProgress(user, userHabits);
                case "alice_novice" -> createNoviceProgress(user, userHabits);
                case "tom_sportif" -> createSportProgress(user, userHabits);
                case "emma_studieuse" -> createStudyProgress(user, userHabits);
                case "jules_intermittent" -> createIntermittentProgress(user, userHabits);
                default -> new ArrayList<>();
            };

            allProgress.addAll(userProgress);
        }

        List<Progress> savedProgress = progressRepository.saveAll(allProgress);

        log.info("✅ {} progressions créées :", savedProgress.size());
        users.forEach(user -> {
            if (user.getRole() != User.Role.ADMIN && user.getIsActive()) {
                long userProgressCount = savedProgress.stream()
                        .filter(p -> p.getUserId().equals(user.getId()))
                        .count();
                log.info("   📈 {} : {} progressions", user.getUsername(), userProgressCount);
            }
        });
    }

    // === PATTERNS DE PROGRESSION ===

    private List<Progress> createChampionProgress(User user, List<Habit> habits) {
        List<Progress> progressList = new ArrayList<>();
        int daysOfActivity = 140; // Très active depuis longtemps

        for (Habit habit : habits) {
            // 95% de régularité
            for (int dayOffset = daysOfActivity; dayOffset >= 0; dayOffset--) {
                if (random.nextDouble() < 0.95) { // Très régulière
                    LocalDate date = LocalDate.now().minusDays(dayOffset);
                    Double value = generateChampionValue(habit, dayOffset, daysOfActivity);
                    String note = dayOffset < 7 ? generateMotivationalNote() : null;

                    progressList.add(createProgress(user.getId(), habit.getId(), date, value, note));
                }
            }
        }
        return progressList;
    }

    private List<Progress> createRegularProgress(User user, List<Habit> habits) {
        List<Progress> progressList = new ArrayList<>();
        int daysOfActivity = 80;

        for (Habit habit : habits) {
            // 75% de régularité avec pattern week-end plus faible
            for (int dayOffset = daysOfActivity; dayOffset >= 0; dayOffset--) {
                LocalDate date = LocalDate.now().minusDays(dayOffset);
                boolean isWeekend = date.getDayOfWeek().getValue() >= 6;

                double consistency = isWeekend ? 0.6 : 0.8; // Moins régulier le week-end

                if (random.nextDouble() < consistency) {
                    Double value = generateRegularValue(habit);
                    progressList.add(createProgress(user.getId(), habit.getId(), date, value, null));
                }
            }
        }
        return progressList;
    }

    private List<Progress> createNoviceProgress(User user, List<Habit> habits) {
        List<Progress> progressList = new ArrayList<>();
        int daysOfActivity = 25; // Nouvelle utilisatrice

        for (Habit habit : habits) {
            // Pattern débutant : start faible, puis amélioration
            for (int dayOffset = daysOfActivity; dayOffset >= 0; dayOffset--) {
                LocalDate date = LocalDate.now().minusDays(dayOffset);

                // Amélioration de la consistance au fil du temps
                double baseConsistency = 0.3 + (0.4 * (daysOfActivity - dayOffset) / daysOfActivity);

                if (random.nextDouble() < baseConsistency) {
                    Double value = generateNoviceValue(habit, dayOffset, daysOfActivity);
                    String note = dayOffset < 5 ? generateBeginnerNote() : null;

                    progressList.add(createProgress(user.getId(), habit.getId(), date, value, note));
                }
            }
        }
        return progressList;
    }

    private List<Progress> createSportProgress(User user, List<Habit> habits) {
        List<Progress> progressList = new ArrayList<>();
        int daysOfActivity = 110;

        for (Habit habit : habits) {
            // Pattern sportif : très régulier avec pics et récupération
            for (int dayOffset = daysOfActivity; dayOffset >= 0; dayOffset--) {
                LocalDate date = LocalDate.now().minusDays(dayOffset);

                // Cycle d'entraînement : 6 jours actifs, 1 jour repos
                boolean isRestDay = (dayOffset % 7) == 0;
                double consistency = isRestDay ? 0.3 : 0.9;

                if (random.nextDouble() < consistency) {
                    Double value = generateSportValue(habit, dayOffset);
                    String note = generateSportNote(habit, dayOffset);

                    progressList.add(createProgress(user.getId(), habit.getId(), date, value, note));
                }
            }
        }
        return progressList;
    }

    private List<Progress> createStudyProgress(User user, List<Habit> habits) {
        List<Progress> progressList = new ArrayList<>();
        int daysOfActivity = 70;

        for (Habit habit : habits) {
            for (int dayOffset = daysOfActivity; dayOffset >= 0; dayOffset--) {
                LocalDate date = LocalDate.now().minusDays(dayOffset);
                boolean isWeekDay = date.getDayOfWeek().getValue() <= 5;

                // Plus active en semaine
                double consistency = isWeekDay ? 0.85 : 0.4;

                if (random.nextDouble() < consistency) {
                    Double value = generateStudyValue(habit);
                    progressList.add(createProgress(user.getId(), habit.getId(), date, value, null));
                }
            }
        }
        return progressList;
    }

    private List<Progress> createIntermittentProgress(User user, List<Habit> habits) {
        List<Progress> progressList = new ArrayList<>();
        int daysOfActivity = 50;

        for (Habit habit : habits) {
            for (int dayOffset = daysOfActivity; dayOffset >= 0; dayOffset--) {
                // Pattern intermittent : bonnes périodes alternées avec abandon
                boolean isInGoodPeriod = ((dayOffset / 10) % 2) == 0;
                double consistency = isInGoodPeriod ? 0.6 : 0.15;

                if (random.nextDouble() < consistency) {
                    Double value = generateBasicValue(habit);
                    progressList.add(createProgress(user.getId(), habit.getId(),
                            LocalDate.now().minusDays(dayOffset), value, null));
                }
            }
        }
        return progressList;
    }

    // === GÉNÉRATEURS DE VALEURS ===

    private Double generateChampionValue(Habit habit, int dayOffset, int totalDays) {
        Double target = habit.getTargetValue();
        if (target == null) return 1.0;

        // Progression dans le temps + variation journalière
        double progressFactor = 0.7 + (0.4 * (totalDays - dayOffset) / totalDays);
        double dailyVariation = 0.9 + (random.nextDouble() * 0.3); // ±15%

        return Math.round(target * progressFactor * dailyVariation * 10.0) / 10.0;
    }

    private Double generateRegularValue(Habit habit) {
        Double target = habit.getTargetValue();
        if (target == null) return 1.0;

        // 80-110% de l'objectif
        double factor = 0.8 + (random.nextDouble() * 0.3);
        return Math.round(target * factor * 10.0) / 10.0;
    }

    private Double generateNoviceValue(Habit habit, int dayOffset, int totalDays) {
        Double target = habit.getTargetValue();
        if (target == null) return 1.0;

        // Commence à 30%, progresse vers 80%
        double progressFactor = 0.3 + (0.5 * (totalDays - dayOffset) / totalDays);
        return Math.round(target * progressFactor * 10.0) / 10.0;
    }

    private Double generateSportValue(Habit habit, int dayOffset) {
        Double target = habit.getTargetValue();
        if (target == null) return 1.0;

        // Performance variable avec pics
        boolean isPeakDay = (dayOffset % 14) < 3; // Pics tous les 2 semaines
        double factor = isPeakDay ?
                1.1 + (random.nextDouble() * 0.3) : // Performance élevée
                0.8 + (random.nextDouble() * 0.4);  // Performance normale

        return Math.round(target * factor * 10.0) / 10.0;
    }

    private Double generateStudyValue(Habit habit) {
        Double target = habit.getTargetValue();
        if (target == null) return 1.0;

        // Régulier autour de l'objectif
        double factor = 0.9 + (random.nextDouble() * 0.2);
        return Math.round(target * factor * 10.0) / 10.0;
    }

    private Double generateBasicValue(Habit habit) {
        Double target = habit.getTargetValue();
        if (target == null) return 1.0;

        // Valeurs basiques
        double factor = 0.5 + (random.nextDouble() * 0.5);
        return Math.round(target * factor * 10.0) / 10.0;
    }

    // === GÉNÉRATEURS DE NOTES ===

    private String generateMotivationalNote() {
        String[] notes = {
                "Excellent ! Dépassé l'objectif 🚀",
                "Superbe séance aujourd'hui",
                "Je sens vraiment la progression",
                "Motivation au top !",
                "Nouvelle routine bien intégrée"
        };
        return notes[random.nextInt(notes.length)];
    }

    private String generateBeginnerNote() {
        String[] notes = {
                "Première fois que j'y arrive !",
                "C'est dur mais j'y vais",
                "Petit à petit...",
                "Plus facile qu'hier",
                "Je commence à prendre le rythme"
        };
        return notes[random.nextInt(notes.length)];
    }

    private String generateSportNote(Habit habit, int dayOffset) {
        if (random.nextDouble() > 0.3) return null; // 30% de chance d'avoir une note

        String[] notes = {
                "PB aujourd'hui ! 💪",
                "Excellente forme",
                "Récupération active",
                "Intensité maximale",
                "Séance parfaite",
                "Nouveau record personnel",
                "Endurance en progrès"
        };
        return notes[random.nextInt(notes.length)];
    }

    private Progress createProgress(Long userId, Long habitId, LocalDate date, Double value, String note) {
        Progress progress = new Progress(userId, habitId, date, value);
        if (note != null && random.nextDouble() < 0.4) { // 40% de chance si note fournie
            progress.setNote(note);
        }
        return progress;
    }

    // =====================================================================
    // ÉTAPE 4 : CRÉATION D'ACHIEVEMENTS RÉALISTES
    // =====================================================================

    private void createMeaningfulAchievements(List<User> users) {
        log.info("🏆 Création d'achievements réalistes...");

        List<Achievement> allAchievements = new ArrayList<>();

        for (User user : users) {
            if (user.getRole() == User.Role.ADMIN || !user.getIsActive()) {
                continue; // Skip admin et inactifs
            }

            List<Achievement> userAchievements = switch (user.getUsername()) {
                case "marie_champion" -> createChampionAchievements(user.getId());
                case "paul_regulier" -> createRegularAchievements(user.getId());
                case "alice_novice" -> createNoviceAchievements(user.getId());
                case "tom_sportif" -> createSportAchievements(user.getId());
                case "emma_studieuse" -> createStudyAchievements(user.getId());
                case "jules_intermittent" -> createBasicAchievements(user.getId());
                default -> new ArrayList<>();
            };

            allAchievements.addAll(userAchievements);
        }

        List<Achievement> savedAchievements = achievementRepository.saveAll(allAchievements);

        log.info("✅ {} achievements créés :", savedAchievements.size());
        users.forEach(user -> {
            if (user.getRole() != User.Role.ADMIN && user.getIsActive()) {
                long userAchievementCount = savedAchievements.stream()
                        .filter(a -> a.getUserId().equals(user.getId()))
                        .count();
                log.info("   🏆 {} : {} achievements", user.getUsername(), userAchievementCount);
            }
        });
    }

    // === PROFILS D'ACHIEVEMENTS ===

    private List<Achievement> createChampionAchievements(Long userId) {
        List<Achievement> achievements = new ArrayList<>();

        // Milestones
        achievements.add(createAchievementWithDate(userId, "Bienvenue ! 🎉",
                "Premier objectif créé", "🎯", Achievement.AchievementType.MILESTONE, 140));
        achievements.add(createAchievementWithDate(userId, "Centurion",
                "100 progressions enregistrées", "💯", Achievement.AchievementType.MILESTONE, 100));
        achievements.add(createAchievementWithDate(userId, "Demi-millénaire",
                "500 progressions enregistrées", "🌟", Achievement.AchievementType.MILESTONE, 60));
        achievements.add(createAchievementWithDate(userId, "Machine à Progresser",
                "1000 progressions enregistrées", "🤖", Achievement.AchievementType.MILESTONE, 20));

        // Consistency
        achievements.add(createAchievementWithDate(userId, "Semaine parfaite",
                "7 jours consécutifs", "🔥", Achievement.AchievementType.CONSISTENCY, 130));
        achievements.add(createAchievementWithDate(userId, "Mois de légende",
                "30 jours consécutifs", "🏅", Achievement.AchievementType.CONSISTENCY, 100));
        achievements.add(createAchievementWithDate(userId, "Trimestre de titan",
                "90 jours consécutifs", "👑", Achievement.AchievementType.CONSISTENCY, 70));

        // Streak
        achievements.add(createAchievementWithDate(userId, "Série impressionnante",
                "15 jours de suite", "⚡", Achievement.AchievementType.STREAK, 120));
        achievements.add(createAchievementWithDate(userId, "Invincible",
                "50 jours de suite", "🛡️", Achievement.AchievementType.STREAK, 80));

        // Overachiever
        achievements.add(createAchievementWithDate(userId, "Surpassement",
                "Objectif dépassé de 50%", "🚀", Achievement.AchievementType.OVERACHIEVER, 110));
        achievements.add(createAchievementWithDate(userId, "Perfectionniste",
                "Objectif dépassé 10 fois", "💎", Achievement.AchievementType.OVERACHIEVER, 90));

        // Variety
        achievements.add(createAchievementWithDate(userId, "Polyvalente",
                "5 catégories différentes", "🌈", Achievement.AchievementType.VARIETY, 125));
        achievements.add(createAchievementWithDate(userId, "Maître de tous",
                "Excellence dans toutes les catégories", "🎭", Achievement.AchievementType.VARIETY, 50));

        // Dedication
        achievements.add(createAchievementWithDate(userId, "Multi-tâches",
                "8 habitudes actives", "🎪", Achievement.AchievementType.DEDICATION, 115));
        achievements.add(createAchievementWithDate(userId, "Super Woman",
                "Excellence sur tous les fronts", "🦸‍♀️", Achievement.AchievementType.DEDICATION, 40));

        return achievements;
    }

    private List<Achievement> createRegularAchievements(Long userId) {
        List<Achievement> achievements = new ArrayList<>();

        // Basics
        achievements.add(createAchievementWithDate(userId, "Bienvenue ! 🎉",
                "Premier objectif créé", "🎯", Achievement.AchievementType.MILESTONE, 80));
        achievements.add(createAchievementWithDate(userId, "Premier pas",
                "Première progression", "👟", Achievement.AchievementType.MILESTONE, 79));
        achievements.add(createAchievementWithDate(userId, "Cinquantaine",
                "50 progressions", "🏁", Achievement.AchievementType.MILESTONE, 50));

        // Consistency modérée
        achievements.add(createAchievementWithDate(userId, "Semaine parfaite",
                "7 jours consécutifs", "🔥", Achievement.AchievementType.CONSISTENCY, 60));
        achievements.add(createAchievementWithDate(userId, "Régularité",
                "Constance sur 2 semaines", "⏰", Achievement.AchievementType.CONSISTENCY, 40));

        // Quelques streaks
        achievements.add(createAchievementWithDate(userId, "Série prometteuse",
                "10 jours de suite", "📈", Achievement.AchievementType.STREAK, 55));

        // Variety
        achievements.add(createAchievementWithDate(userId, "Équilibre",
                "3 catégories différentes", "⚖️", Achievement.AchievementType.VARIETY, 70));

        return achievements;
    }

    private List<Achievement> createNoviceAchievements(Long userId) {
        List<Achievement> achievements = new ArrayList<>();

        // Premiers pas
        achievements.add(createAchievementWithDate(userId, "Bienvenue ! 🎉",
                "Premier objectif créé", "🎯", Achievement.AchievementType.MILESTONE, 25));
        achievements.add(createAchievementWithDate(userId, "Premier pas",
                "Première progression", "👟", Achievement.AchievementType.MILESTONE, 24));
        achievements.add(createAchievementWithDate(userId, "Dixaine",
                "10 progressions", "🔟", Achievement.AchievementType.MILESTONE, 15));

        // Premières séries
        achievements.add(createAchievementWithDate(userId, "Trois de suite",
                "3 jours consécutifs", "3️⃣", Achievement.AchievementType.STREAK, 20));
        achievements.add(createAchievementWithDate(userId, "Une semaine !",
                "7 jours consécutifs", "🔥", Achievement.AchievementType.CONSISTENCY, 10));

        // Encouragement
        achievements.add(createAchievementWithDate(userId, "Persévérance",
                "Continue malgré les difficultés", "💪", Achievement.AchievementType.PERSEVERANCE, 18));

        return achievements;
    }

    private List<Achievement> createSportAchievements(Long userId) {
        List<Achievement> achievements = new ArrayList<>();

        // Milestones sportifs
        achievements.add(createAchievementWithDate(userId, "Bienvenue ! 🎉",
                "Premier objectif créé", "🎯", Achievement.AchievementType.MILESTONE, 110));
        achievements.add(createAchievementWithDate(userId, "Athlète",
                "100 séances d'entraînement", "🏃‍♂️", Achievement.AchievementType.MILESTONE, 80));
        achievements.add(createAchievementWithDate(userId, "Machine de guerre",
                "500 progressions sportives", "⚔️", Achievement.AchievementType.MILESTONE, 40));

        // Consistency sportive
        achievements.add(createAchievementWithDate(userId, "Discipline de fer",
                "30 jours consécutifs", "🔥", Achievement.AchievementType.CONSISTENCY, 85));
        achievements.add(createAchievementWithDate(userId, "Régularité olympique",
                "60 jours consécutifs", "🥇", Achievement.AchievementType.CONSISTENCY, 60));

        // Performance
        achievements.add(createAchievementWithDate(userId, "Beast Mode",
                "Objectif dépassé de 100%", "🦍", Achievement.AchievementType.OVERACHIEVER, 75));
        achievements.add(createAchievementWithDate(userId, "Record battu",
                "Performance exceptionnelle", "📊", Achievement.AchievementType.OVERACHIEVER, 50));

        // Dedication sportive
        achievements.add(createAchievementWithDate(userId, "Sportif complet",
                "Excellence multi-sports", "🏆", Achievement.AchievementType.DEDICATION, 70));

        // Early bird
        achievements.add(createAchievementWithDate(userId, "Lève-tôt",
                "Entraînement matinal", "🌅", Achievement.AchievementType.EARLY_BIRD, 90));

        return achievements;
    }

    private List<Achievement> createStudyAchievements(Long userId) {
        List<Achievement> achievements = new ArrayList<>();

        // Éducation
        achievements.add(createAchievementWithDate(userId, "Bienvenue ! 🎉",
                "Premier objectif créé", "🎯", Achievement.AchievementType.MILESTONE, 70));
        achievements.add(createAchievementWithDate(userId, "Étudiante modèle",
                "50 sessions d'étude", "📚", Achievement.AchievementType.MILESTONE, 50));
        achievements.add(createAchievementWithDate(userId, "Intellectuelle",
                "200 heures d'apprentissage", "🧠", Achievement.AchievementType.MILESTONE, 30));

        // Consistency d'étude
        achievements.add(createAchievementWithDate(userId, "Assidue",
                "15 jours consécutifs d'étude", "📖", Achievement.AchievementType.CONSISTENCY, 55));
        achievements.add(createAchievementWithDate(userId, "Studieuse",
                "Un mois d'apprentissage", "🎓", Achievement.AchievementType.CONSISTENCY, 35));

        // Variety éducative
        achievements.add(createAchievementWithDate(userId, "Polyglotte",
                "Apprentissage multi-langues", "🌍", Achievement.AchievementType.VARIETY, 60));
        achievements.add(createAchievementWithDate(userId, "Curieuse",
                "Diversité des apprentissages", "🔍", Achievement.AchievementType.VARIETY, 45));

        return achievements;
    }

    private List<Achievement> createBasicAchievements(Long userId) {
        List<Achievement> achievements = new ArrayList<>();

        achievements.add(createAchievementWithDate(userId, "Bienvenue ! 🎉",
                "Premier objectif créé", "🎯", Achievement.AchievementType.MILESTONE, 50));
        achievements.add(createAchievementWithDate(userId, "Premier pas",
                "Première progression", "👟", Achievement.AchievementType.MILESTONE, 49));
        achievements.add(createAchievementWithDate(userId, "Tentative",
                "3 jours d'efforts", "🤞", Achievement.AchievementType.STREAK, 40));

        return achievements;
    }

    private Achievement createAchievementWithDate(Long userId, String name, String description,
                                                  String icon, Achievement.AchievementType type, int daysAgo) {
        Achievement achievement = new Achievement(userId, name, description, icon, type);
        LocalDateTime unlockedAt = LocalDateTime.now().minusDays(daysAgo);
        achievement.setUnlockedAt(unlockedAt);
        return achievement;
    }

    // =====================================================================
    // ÉTAPE 5 : STATISTIQUES DÉTAILLÉES FINALES
    // =====================================================================

    private void logDetailedStatistics() {
        log.info("📊 === STATISTIQUES DÉTAILLÉES FINALES ===");

        // Stats globales
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActive(true);
        long inactiveUsers = userRepository.countByIsActive(false);
        long totalHabits = habitRepository.count();
        long activeHabits = habitRepository.countByIsActive(true);
        long totalProgress = progressRepository.count();
        long totalAchievements = achievementRepository.count();

        log.info("👥 UTILISATEURS :");
        log.info("   Total : {} | Actifs : {} | Inactifs : {}", totalUsers, activeUsers, inactiveUsers);

        log.info("🎯 HABITUDES :");
        log.info("   Total : {} | Actives : {}", totalHabits, activeHabits);

        log.info("📈 PROGRESSIONS : {}", totalProgress);
        log.info("🏆 ACHIEVEMENTS : {}", totalAchievements);

        // Stats par utilisateur actif
        log.info("\n📊 DÉTAIL PAR UTILISATEUR ACTIF :");
        userRepository.findByIsActive(true, org.springframework.data.domain.Pageable.unpaged())
                .forEach(user -> {
                    if (user.getRole() != User.Role.ADMIN) {
                        long userHabits = habitRepository.countByUserIdAndIsActive(user.getId(), true);
                        long userProgress = progressRepository.countByUserId(user.getId());
                        long userAchievements = achievementRepository.countByUserId(user.getId());
                        long daysSinceJoin = java.time.temporal.ChronoUnit.DAYS.between(
                                user.getCreatedAt().toLocalDate(), LocalDate.now());

                        log.info("   👤 {} ({}j) : {} hab. | {} prog. | {} badges",
                                user.getUsername(), daysSinceJoin, userHabits, userProgress, userAchievements);
                    }
                });

        // Stats par catégorie d'habitudes
        log.info("\n🏷️ RÉPARTITION PAR CATÉGORIE :");
        for (Habit.Category category : Habit.Category.values()) {
            long count = habitRepository.findAll().stream()
                    .filter(h -> h.getCategory() == category && h.getIsActive())
                    .count();
            if (count > 0) {
                log.info("   {} : {} habitudes", category, count);
            }
        }

        // Stats par type d'achievement
        log.info("\n🏆 RÉPARTITION ACHIEVEMENTS :");
        for (Achievement.AchievementType type : Achievement.AchievementType.values()) {
            long count = achievementRepository.findAll().stream()
                    .filter(a -> a.getAchievementType() == type)
                    .count();
            if (count > 0) {
                log.info("   {} : {} badges", type, count);
            }
        }

        // Moyennes et insights
        log.info("\n📊 INSIGHTS :");
        if (activeUsers > 0) {
            double avgHabitsPerUser = (double) activeHabits / (activeUsers - 1); // -1 pour exclure admin
            double avgProgressPerUser = (double) totalProgress / (activeUsers - 1);
            double avgAchievementsPerUser = (double) totalAchievements / (activeUsers - 1);

            log.info("   📈 Moyenne par utilisateur actif :");
            log.info("      Habitudes : {}", Math.round(avgHabitsPerUser * 10.0) / 10.0);
            log.info("      Progressions : {}", Math.round(avgProgressPerUser * 10.0) / 10.0);
            log.info("      Achievements : {}", Math.round(avgAchievementsPerUser * 10.0) / 10.0);
        }

        // Utilisateurs les plus actifs
        log.info("\n🥇 TOP UTILISATEURS :");
        userRepository.findByIsActive(true, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(user -> user.getRole() != User.Role.ADMIN)
                .sorted((u1, u2) -> {
                    long progress1 = progressRepository.countByUserId(u1.getId());
                    long progress2 = progressRepository.countByUserId(u2.getId());
                    return Long.compare(progress2, progress1);
                })
                .limit(3)
                .forEach(user -> {
                    long userProgress = progressRepository.countByUserId(user.getId());
                    long userAchievements = achievementRepository.countByUserId(user.getId());
                    log.info("   🏆 {} : {} progressions, {} achievements",
                            user.getUsername(), userProgress, userAchievements);
                });

        // Période d'activité
        LocalDate oldestProgress = progressRepository.findAll().stream()
                .map(Progress::getDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate newestProgress = progressRepository.findAll().stream()
                .map(Progress::getDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        long activityPeriodDays = java.time.temporal.ChronoUnit.DAYS.between(oldestProgress, newestProgress);

        log.info("\n📅 PÉRIODE D'ACTIVITÉ :");
        log.info("   Du {} au {} ({} jours)", oldestProgress, newestProgress, activityPeriodDays);

        if (activityPeriodDays > 0) {
            double avgProgressPerDay = (double) totalProgress / activityPeriodDays;
            log.info("   Moyenne : {} progressions/jour", Math.round(avgProgressPerDay * 10.0) / 10.0);
        }

        log.info("\n🎯 DONNÉES PRÊTES POUR LES TESTS ! 🎯");
        log.info("================================================");
    }

    // =====================================================================
    // MÉTHODES UTILITAIRES SUPPLÉMENTAIRES
    // =====================================================================

    /**
     * Méthode pour ajouter encore plus de données si besoin
     */
    public void addMoreTestData() {
        log.info("🔄 Ajout de données supplémentaires...");

        // Ajouter quelques progressions récentes pour tous les utilisateurs actifs
        userRepository.findByIsActive(true, org.springframework.data.domain.Pageable.unpaged())
                .forEach(user -> {
                    if (user.getRole() != User.Role.ADMIN) {
                        addRecentProgressForUser(user.getId());
                    }
                });
    }

    /**
     * Ajoute des progressions récentes pour un utilisateur
     */
    private void addRecentProgressForUser(Long userId) {
        List<Habit> userHabits = habitRepository.findByUserIdAndIsActive(userId, true);
        List<Progress> recentProgress = new ArrayList<>();

        // Progressions pour les 3 derniers jours
        for (int dayOffset = 2; dayOffset >= 0; dayOffset--) {
            LocalDate date = LocalDate.now().minusDays(dayOffset);

            for (Habit habit : userHabits) {
                // 70% de chance d'avoir une progression
                if (random.nextDouble() < 0.7) {
                    if (!progressRepository.existsByUserIdAndHabitIdAndDate(userId, habit.getId(), date)) {
                        Double value = generateBasicValue(habit);
                        String note = dayOffset == 0 ? "Progression du jour !" : null;

                        recentProgress.add(createProgress(userId, habit.getId(), date, value, note));
                    }
                }
            }
        }

        if (!recentProgress.isEmpty()) {
            progressRepository.saveAll(recentProgress);
            log.info("   ➕ {} progressions récentes ajoutées pour user {}",
                    recentProgress.size(), userId);
        }
    }
}