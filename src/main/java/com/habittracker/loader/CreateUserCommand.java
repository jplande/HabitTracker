package com.habittracker.loader;

import com.habittracker.entity.User;
import com.habittracker.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@Slf4j
public class CreateUserCommand implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // Vérifier si des arguments de création d'utilisateur sont passés
        if (args.containsOption("create-user")) {
            createUserFromArgs(args);
        }

        if (args.containsOption("create-admin")) {
            createAdminFromArgs(args);
        }
    }

    /**
     * Crée un utilisateur depuis les arguments de ligne de commande
     * Usage: --create-user --username=john --email=john@test.com --password=test123
     */
    private void createUserFromArgs(ApplicationArguments args) {
        try {
            String username = getArgValue(args, "username");
            String email = getArgValue(args, "email");
            String password = getArgValue(args, "password");
            String firstName = args.getOptionValues("firstname") != null ?
                    args.getOptionValues("firstname").get(0) : null;
            String lastName = args.getOptionValues("lastname") != null ?
                    args.getOptionValues("lastname").get(0) : null;

            if (username == null || email == null || password == null) {
                log.error("❌ Arguments manquants. Usage: --create-user --username=xxx --email=xxx --password=xxx");
                return;
            }

            // Vérifier que l'utilisateur n'existe pas
            if (userRepository.existsByUsername(username)) {
                log.error("❌ L'utilisateur '{}' existe déjà", username);
                return;
            }

            if (userRepository.existsByEmail(email)) {
                log.error("❌ L'email '{}' est déjà utilisé", email);
                return;
            }

            // Créer l'utilisateur
            User user = new User(username, email, password);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRole(User.Role.USER);

            user = userRepository.save(user);
            log.info("✅ Utilisateur créé : {} ({})", user.getUsername(), user.getEmail());

        } catch (Exception e) {
            log.error("❌ Erreur lors de la création de l'utilisateur : {}", e.getMessage());
        }
    }

    /**
     * Crée un administrateur depuis les arguments de ligne de commande
     * Usage: --create-admin --username=admin --email=admin@test.com --password=admin123
     */
    private void createAdminFromArgs(ApplicationArguments args) {
        try {
            String username = getArgValue(args, "username");
            String email = getArgValue(args, "email");
            String password = getArgValue(args, "password");

            if (username == null || email == null || password == null) {
                log.error("❌ Arguments manquants pour admin. Usage: --create-admin --username=xxx --email=xxx --password=xxx");
                return;
            }

            if (userRepository.existsByUsername(username)) {
                log.error("❌ L'administrateur '{}' existe déjà", username);
                return;
            }

            User admin = new User(username, email, password);
            admin.setRole(User.Role.ADMIN);
            admin.setFirstName("Admin");
            admin.setLastName("System");

            admin = userRepository.save(admin);
            log.info("✅ Administrateur créé : {} ({})", admin.getUsername(), admin.getEmail());

        } catch (Exception e) {
            log.error("❌ Erreur lors de la création de l'administrateur : {}", e.getMessage());
        }
    }

    private String getArgValue(ApplicationArguments args, String key) {
        return args.getOptionValues(key) != null ? args.getOptionValues(key).get(0) : null;
    }
}