package com.habittracker.service;

import com.habittracker.entity.User;
import com.habittracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'initialisation pour créer un utilisateur admin par défaut
 * SIMPLIFIÉ pour éviter les conflits de démarrage
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Order(100) // Exécuter après les autres initialisations
public class AdminInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            createDefaultAdminIfNotExists();
        } catch (Exception e) {
            log.error("❌ Erreur lors de la création de l'admin par défaut: {}", e.getMessage());
        }
    }

    /**
     * Crée un utilisateur admin par défaut s'il n'existe pas
     */
    private void createDefaultAdminIfNotExists() {
        // Vérifier s'il existe déjà un admin
        boolean adminExists = userRepository.findByUsername("admin").isPresent();

        if (!adminExists) {
            log.info("🔧 Création de l'utilisateur admin par défaut...");

            User admin = new User("admin", "admin@habittracker.com",
                    passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("System");
            admin.setRole(User.Role.ADMIN);
            admin.setIsActive(true);

            userRepository.save(admin);

            log.info("✅ Utilisateur admin créé avec succès !");
            log.info("   🔑 Username: admin");
            log.info("   🔑 Password: admin123");
            log.info("   ⚠️  CHANGEZ CE MOT DE PASSE EN PRODUCTION !");

        } else {
            log.info("ℹ️  Utilisateur admin déjà existant.");
        }

        // Afficher les infos des admins existants
        logExistingAdmins();
    }

    /**
     * Affiche les informations sur les comptes admin existants
     */
    private void logExistingAdmins() {
        long adminCount = userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.ADMIN)
                .count();

        log.info("📋 {} administrateur(s) dans le système", adminCount);

        userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.ADMIN)
                .forEach(admin -> {
                    log.info("   👤 Admin: {} ({}) - Actif: {}",
                            admin.getUsername(),
                            admin.getEmail(),
                            admin.getIsActive() ? "✅" : "❌"
                    );
                });
    }

    /**
     * Méthode utilitaire publique pour vérifier si un admin existe
     */
    public boolean hasAdminUser() {
        return userRepository.findAll().stream()
                .anyMatch(user -> user.getRole() == User.Role.ADMIN && user.getIsActive());
    }

    /**
     * Méthode pour promouvoir un utilisateur existant en admin
     */
    @Transactional
    public boolean promoteUserToAdmin(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setRole(User.Role.ADMIN);
                    userRepository.save(user);
                    log.info("🔧 Utilisateur {} promu au rang d'administrateur", username);
                    return true;
                })
                .orElse(false);
    }
}