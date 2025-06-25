package com.habittracker.service;

import com.habittracker.dto.habit.HabitCreateRequest;
import com.habittracker.dto.habit.HabitResponse;
import com.habittracker.dto.habit.HabitUpdateRequest;
import com.habittracker.entity.Habit;
import com.habittracker.exception.BusinessException;
import com.habittracker.exception.ResourceNotFoundException;
import com.habittracker.repository.HabitRepository;
import com.habittracker.repository.ProgressRepository;
import com.habittracker.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HabitService {

    private final HabitRepository habitRepository;
    private final ProgressRepository progressRepository;

    /**
     * Trouve une habitude par son ID
     */
    public HabitResponse findById(Long id, Long userId) {
        ValidationUtils.validateId(id, "habitude");
        ValidationUtils.validateId(userId, "utilisateur");

        Habit habit = habitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitude", id));

        // Vérifier que l'habitude appartient à l'utilisateur
        if (!habit.getUserId().equals(userId)) {
            throw new BusinessException("Cette habitude ne vous appartient pas");
        }

        return enrichWithStatistics(HabitResponse.fromEntity(habit));
    }

    /**
     * Liste toutes les habitudes d'un utilisateur avec pagination
     */
    public Page<HabitResponse> findAllByUser(Long userId, Pageable pageable) {
        ValidationUtils.validateId(userId, "utilisateur");

        return habitRepository.findByUserId(userId, pageable)
                .map(HabitResponse::fromEntity);
    }

    /**
     * Liste les habitudes actives d'un utilisateur
     */
    public Page<HabitResponse> findActiveByUser(Long userId, Pageable pageable) {
        ValidationUtils.validateId(userId, "utilisateur");

        return habitRepository.findByUserIdAndIsActive(userId, true, pageable)
                .map(HabitResponse::fromEntity);
    }

    /**
     * Recherche les habitudes par titre
     */
    public Page<HabitResponse> searchByTitle(Long userId, String title, Pageable pageable) {
        ValidationUtils.validateId(userId, "utilisateur");
        ValidationUtils.validateNotEmpty(title, "Titre de recherche");

        return habitRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title, pageable)
                .map(HabitResponse::fromEntity);
    }

    /**
     * Filtre les habitudes par catégorie
     */
    public Page<HabitResponse> findByCategory(Long userId, Habit.Category category, Pageable pageable) {
        ValidationUtils.validateId(userId, "utilisateur");
        ValidationUtils.validateNotNull(category, "Catégorie");

        return habitRepository.findByUserIdAndCategory(userId, category, pageable)
                .map(HabitResponse::fromEntity);
    }

    /**
     * Crée une nouvelle habitude
     */
    @Transactional
    public HabitResponse createHabit(Long userId, HabitCreateRequest request) {
        ValidationUtils.validateId(userId, "utilisateur");
        validateHabitCreationRequest(request);

        Habit habit = buildHabitFromRequest(userId, request);
        habit = habitRepository.save(habit);

        log.info("Habitude créée: {} pour l'utilisateur {}", habit.getTitle(), userId);
        return HabitResponse.fromEntity(habit);
    }

    /**
     * Met à jour une habitude existante
     */
    @Transactional
    public HabitResponse updateHabit(Long id, Long userId, HabitUpdateRequest request) {
        ValidationUtils.validateId(id, "habitude");
        ValidationUtils.validateId(userId, "utilisateur");

        if (request.isEmpty()) {
            throw new BusinessException("Aucune modification fournie");
        }

        Habit habit = findHabitEntityById(id);

        // Vérifier que l'habitude appartient à l'utilisateur
        if (!habit.getUserId().equals(userId)) {
            throw new BusinessException("Cette habitude ne vous appartient pas");
        }

        updateHabitFields(habit, request);
        habit = habitRepository.save(habit);

        log.info("Habitude mise à jour: {} pour l'utilisateur {}", habit.getTitle(), userId);
        return enrichWithStatistics(HabitResponse.fromEntity(habit));
    }

    /**
     * Supprime une habitude (soft delete)
     */
    @Transactional
    public void deleteHabit(Long id, Long userId) {
        ValidationUtils.validateId(id, "habitude");
        ValidationUtils.validateId(userId, "utilisateur");

        Habit habit = findHabitEntityById(id);

        // Vérifier que l'habitude appartient à l'utilisateur
        if (!habit.getUserId().equals(userId)) {
            throw new BusinessException("Cette habitude ne vous appartient pas");
        }

        habit.setIsActive(false);
        habitRepository.save(habit);

        log.info("Habitude désactivée: {} pour l'utilisateur {}", habit.getTitle(), userId);
    }

    /**
     * Active/désactive une habitude
     */
    @Transactional
    public HabitResponse toggleHabitStatus(Long id, Long userId) {
        ValidationUtils.validateId(id, "habitude");
        ValidationUtils.validateId(userId, "utilisateur");

        Habit habit = findHabitEntityById(id);

        // Vérifier que l'habitude appartient à l'utilisateur
        if (!habit.getUserId().equals(userId)) {
            throw new BusinessException("Cette habitude ne vous appartient pas");
        }

        habit.setIsActive(!habit.getIsActive());
        habit = habitRepository.save(habit);

        log.info("Statut de l'habitude changé: {} -> {} pour l'utilisateur {}",
                habit.getTitle(), habit.getIsActive() ? "active" : "inactive", userId);

        return HabitResponse.fromEntity(habit);
    }

    // === MÉTHODES PRIVÉES ===

    /**
     * Trouve une entité Habit par ID
     */
    private Habit findHabitEntityById(Long id) {
        return habitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitude", id));
    }

    /**
     * Enrichit la réponse avec les statistiques
     */
    private HabitResponse enrichWithStatistics(HabitResponse habitResponse) {
        Long habitId = habitResponse.getId();

        // Calcul des statistiques
        Long totalProgress = progressRepository.countByHabitId(habitId);
        Long currentStreak = calculateCurrentStreak(habitId);
        Double averageCompletion = calculateAverageCompletion(habitId);
        LocalDateTime lastProgressDate = getLastProgressDate(habitId);

        return habitResponse.withStatistics(totalProgress, currentStreak, averageCompletion, lastProgressDate);
    }

    /**
     * Calcule la série actuelle d'une habitude
     */
    private Long calculateCurrentStreak(Long habitId) {
        // Implémentation simple - peut être améliorée
        LocalDate today = LocalDate.now();
        long streak = 0;

        for (int i = 0; i < 30; i++) { // Vérifier les 30 derniers jours
            LocalDate checkDate = today.minusDays(i);
            boolean hasProgress = progressRepository.existsByHabitIdAndDate(habitId, checkDate);

            if (hasProgress) {
                streak++;
            } else if (i == 0) {
                // Si pas de progrès aujourd'hui, pas de série
                break;
            } else {
                // Première interruption trouvée
                break;
            }
        }

        return streak;
    }

    /**
     * Calcule la moyenne de completion d'une habitude
     */
    private Double calculateAverageCompletion(Long habitId) {
        // Implémentation simple - peut être améliorée
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<com.habittracker.entity.Progress> progressList =
                progressRepository.findByHabitIdAndDateBetween(habitId, thirtyDaysAgo, LocalDate.now());

        if (progressList.isEmpty()) {
            return 0.0;
        }

        double totalCompletion = progressList.stream()
                .mapToDouble(com.habittracker.entity.Progress::getValue)
                .sum();

        return totalCompletion / progressList.size();
    }

    /**
     * Récupère la date du dernier progrès
     */
    private LocalDateTime getLastProgressDate(Long habitId) {
        return progressRepository.findTop1ByHabitIdOrderByDateDesc(habitId)
                .map(com.habittracker.entity.Progress::getCreatedAt)
                .orElse(null);
    }

    /**
     * Valide la requête de création d'habitude
     */
    private void validateHabitCreationRequest(HabitCreateRequest request) {
        if (request == null) {
            throw new BusinessException("Les données de l'habitude sont requises");
        }

        ValidationUtils.validateNotEmpty(request.getTitle(), "Titre");
        ValidationUtils.validateNotNull(request.getCategory(), "Catégorie");
        ValidationUtils.validateNotEmpty(request.getUnit(), "Unité");
        ValidationUtils.validateNotNull(request.getFrequency(), "Fréquence");

        if (request.getTargetValue() != null) {
            ValidationUtils.validatePositive(request.getTargetValue(), "Valeur cible");
        }
    }

    /**
     * Construit une habitude à partir de la requête
     */
    private Habit buildHabitFromRequest(Long userId, HabitCreateRequest request) {
        Habit habit = new Habit(userId, request.getTitle(), request.getCategory(),
                request.getUnit(), request.getFrequency());

        habit.setDescription(request.getDescription());
        habit.setTargetValue(request.getTargetValue());
        habit.setIsActive(true);

        return habit;
    }

    /**
     * Met à jour les champs modifiables d'une habitude
     */
    private void updateHabitFields(Habit habit, HabitUpdateRequest request) {
        if (StringUtils.hasText(request.getTitle())) {
            habit.setTitle(request.getTitle());
        }

        if (StringUtils.hasText(request.getDescription())) {
            habit.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {
            habit.setCategory(request.getCategory());
        }

        if (StringUtils.hasText(request.getUnit())) {
            habit.setUnit(request.getUnit());
        }

        if (request.getFrequency() != null) {
            habit.setFrequency(request.getFrequency());
        }

        if (request.getTargetValue() != null) {
            habit.setTargetValue(request.getTargetValue());
        }

        if (request.getIsActive() != null) {
            habit.setIsActive(request.getIsActive());
        }
    }
}