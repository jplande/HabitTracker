package com.habittracker.service;

import com.habittracker.dto.UserCreateRequest;
import com.habittracker.dto.UserResponse;
import com.habittracker.dto.UserUpdateRequest;
import com.habittracker.entity.User;
import com.habittracker.exception.BusinessException;
import com.habittracker.exception.ResourceNotFoundException;
import com.habittracker.repository.AchievementRepository;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.ProgressRepository;
import com.habittracker.repository.UserRepository;
import com.habittracker.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
     * Liste tous les utilisateurs avec pagination
     */
    public Page<UserResponse> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::fromEntity);
    }

    /**
     * Liste les utilisateurs actifs
     */
    public Page<UserResponse> findActiveUsers(Pageable pageable) {
        return userRepository.findByIsActive(true, pageable)
                .map(UserResponse::fromEntity);
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
}