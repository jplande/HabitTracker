package com.habittracker.service;

import com.habittracker.dto.user.AdminUserUpdateRequest;
import com.habittracker.dto.user.UserCreateRequest;
import com.habittracker.dto.user.UserResponse;
import com.habittracker.dto.user.UserUpdateRequest;
import com.habittracker.entity.User;
import com.habittracker.exception.BusinessException;
import com.habittracker.exception.ResourceNotFoundException;
import com.habittracker.repository.AchievementRepository;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.ProgressRepository;
import com.habittracker.repository.UserRepository;
import com.habittracker.util.ValidationUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final HabitRepository habitRepository;
    private final ProgressRepository progressRepository;
    private final AchievementRepository achievementRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Trouve un utilisateur par son ID
     */
    public UserResponse findById(Long id) {
        ValidationUtils.validateId(id, "utilisateur");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        return enrichWithStatistics(UserResponse.fromEntity(user));
    }

    /**
     * Trouve un utilisateur par son nom d'utilisateur
     */
    public UserResponse findByUsername(String username) {
        ValidationUtils.validateNotEmpty(username, "Nom d'utilisateur");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur", "username", username));

        return enrichWithStatistics(UserResponse.fromEntity(user));
    }

    /**
     * Crée un nouvel utilisateur
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        validateUserCreationRequest(request);

        // Vérifier l'unicité
        checkUsernameUniqueness(request.getUsername());
        checkEmailUniqueness(request.getEmail());

        // Créer l'utilisateur
        User user = buildUserFromRequest(request);
        user = userRepository.save(user);

        log.info("Utilisateur créé: {} ({})", user.getUsername(), user.getEmail());
        return UserResponse.fromEntity(user);
    }

    /**
     * Met à jour un utilisateur existant
     */
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        ValidationUtils.validateId(id, "utilisateur");

        if (request.isEmpty()) {
            throw new BusinessException("Aucune modification fournie");
        }

        User user = findUserEntityById(id);
        updateUserFields(user, request);

        user = userRepository.save(user);

        log.info("Utilisateur mis à jour: {}", user.getUsername());
        return enrichWithStatistics(UserResponse.fromEntity(user));
    }

    /**
     * Supprime un utilisateur (soft delete)
     */
    @Transactional
    public void deleteUser(Long id) {
        ValidationUtils.validateId(id, "utilisateur");

        User user = findUserEntityById(id);
        user.setIsActive(false);
        userRepository.save(user);

        log.info("Utilisateur désactivé: {}", user.getUsername());
    }

    /**
     * Vérifie si un utilisateur existe
     */
    public boolean existsById(Long id) {
        return id != null && userRepository.existsById(id);
    }

    /**
     * Vérifie si un nom d'utilisateur est disponible
     */
    public boolean isUsernameAvailable(String username) {
        return StringUtils.hasText(username) && !userRepository.existsByUsername(username);
    }

    /**
     * Vérifie si un email est disponible
     */
    public boolean isEmailAvailable(String email) {
        return StringUtils.hasText(email) && !userRepository.existsByEmail(email);
    }

    // === MÉTHODES PRIVÉES ===

    /**
     * Trouve une entité User par ID
     */
    private User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }

    /**
     * Enrichit la réponse avec les statistiques
     */
    private UserResponse enrichWithStatistics(UserResponse userResponse) {
        Long userId = userResponse.getId();

        Long totalHabits = habitRepository.countByUserId(userId);
        Long activeHabits = habitRepository.countByUserIdAndIsActive(userId, true);
        Long totalProgress = progressRepository.countByUserId(userId);
        Long totalAchievements = achievementRepository.countByUserId(userId);

        return userResponse.withStatistics(totalHabits, activeHabits, totalProgress, totalAchievements);
    }

    /**
     * Valide la requête de création d'utilisateur
     */
    private void validateUserCreationRequest(UserCreateRequest request) {
        if (request == null) {
            throw new BusinessException("Les données utilisateur sont requises");
        }

        ValidationUtils.validateUsername(request.getUsername());
        ValidationUtils.validateEmail(request.getEmail());
        ValidationUtils.validateLength(request.getPassword(), "Mot de passe", 8, 100);
    }

    /**
     * Vérifie l'unicité du nom d'utilisateur
     */
    private void checkUsernameUniqueness(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Ce nom d'utilisateur est déjà utilisé: " + username);
        }
    }

    /**
     * Vérifie l'unicité de l'email
     */
    private void checkEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Cet email est déjà utilisé: " + email);
        }
    }

    /**
     * Construit un utilisateur à partir de la requête
     */
    private User buildUserFromRequest(UserCreateRequest request) {
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(User.Role.USER);
        user.setIsActive(true);

        return user;
    }

    /**
     * Met à jour les champs modifiables d'un utilisateur
     */
    private void updateUserFields(User user, UserUpdateRequest request) {
        if (StringUtils.hasText(request.getEmail())) {
            // Vérifier l'unicité si l'email change
            if (!request.getEmail().equals(user.getEmail())) {
                checkEmailUniqueness(request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getFirstName())) {
            user.setFirstName(request.getFirstName());
        }

        if (StringUtils.hasText(request.getLastName())) {
            user.setLastName(request.getLastName());
        }

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            log.info("Mot de passe mis à jour pour: {}", user.getUsername());
        }
    }

    /**
     * Recherche d'utilisateurs par nom d'utilisateur ou email
     */
    public Page<UserResponse> searchUsers(String search, Pageable pageable) {
        log.info("🔍 Recherche utilisateurs : '{}'", search);

        Page<User> users = userRepository.findByUsernameContainingIgnoreCase(search, pageable);
        return users.map(this::convertToResponse);
    }

    /**
     * Trouve les utilisateurs actifs
     */
    public Page<UserResponse> findActiveUsers(Pageable pageable) {
        log.info("👥 Recherche utilisateurs actifs");

        Page<User> users = userRepository.findByIsActive(true, pageable);
        return users.map(this::convertToResponse);
    }

    /**
     * Trouve les utilisateurs inactifs
     */
    public Page<UserResponse> findInactiveUsers(Pageable pageable) {
        log.info("👥 Recherche utilisateurs inactifs");

        Page<User> users = userRepository.findByIsActive(false, pageable);
        return users.map(this::convertToResponse);
    }

    /**
     * Trouve tous les utilisateurs (paginé)
     */
    public Page<UserResponse> findAllUsers(Pageable pageable) {
        log.info("👥 Recherche tous les utilisateurs");

        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToResponse);
    }

    /**
     * Bascule le statut actif/inactif d'un utilisateur
     */
    public UserResponse toggleUserStatus(Long userId) {
        log.info("🔄 Basculement statut utilisateur {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé : " + userId));

        user.setIsActive(!user.getIsActive());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("✅ Statut utilisateur {} basculé vers : {}", userId, savedUser.getIsActive());

        return convertToResponse(savedUser);
    }

    /**
     * Compte total des utilisateurs
     */
    public long getTotalUsersCount() {
        return userRepository.count();
    }

    /**
     * Compte des utilisateurs actifs
     */
    public long getActiveUsersCount() {
        return userRepository.countByIsActive(true);
    }

    /**
     * Méthode de conversion User -> UserResponse (si elle n'existe pas déjà)
     */
    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // Ajouter cette méthode dans UserService

    /**
     * Met à jour un utilisateur par un administrateur
     * Permet de modifier tous les champs y compris rôle et statut
     */
    @Transactional
    public UserResponse updateUserByAdmin(Long userId, AdminUserUpdateRequest request) {
        log.info("🔧 Mise à jour utilisateur {} par administrateur", userId);

        // Récupération de l'utilisateur existant
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        // Vérification unicité username (sauf si inchangé)
        if (!user.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Ce nom d'utilisateur est déjà utilisé : " + request.getUsername());
        }

        // Vérification unicité email (sauf si inchangé)
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Cette adresse email est déjà utilisée : " + request.getEmail());
        }

        // Mise à jour des champs obligatoires
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // Mise à jour des champs optionnels
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        // Mise à jour du rôle (admin seulement)
        if (request.getRole() != null) {
            user.setRole(request.getRole());
            log.info("👑 Rôle utilisateur {} changé vers : {}", userId, request.getRole());
        }

        // Mise à jour du statut actif (admin seulement)
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
            log.info("🔄 Statut utilisateur {} changé vers : {}", userId, request.getIsActive() ? "actif" : "inactif");
        }

        user.setUpdatedAt(LocalDateTime.now());

        // Sauvegarde
        User savedUser = userRepository.save(user);
        log.info("✅ Utilisateur {} mis à jour avec succès par admin", userId);

        return convertToResponse(savedUser);
    }
}