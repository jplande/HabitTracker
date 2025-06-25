package com.habittracker.controller;

import com.habittracker.dto.habit.HabitCreateRequest;
import com.habittracker.dto.habit.HabitResponse;
import com.habittracker.dto.habit.HabitUpdateRequest;
import com.habittracker.entity.Habit;
import com.habittracker.service.HabitSecurityService;
import com.habittracker.service.HabitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
@Slf4j
public class HabitController {

    private final HabitService habitService;
    private final HabitSecurityService habitSecurityService;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<HabitResponse>>> getAllHabits(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Habit.Category category,
            @RequestParam(required = false) Boolean active,
            Authentication authentication) {

        Long currentUserId = habitSecurityService.getCurrentUserId(authentication);

        log.info("Récupération des habitudes pour l'utilisateur: {}", currentUserId);

        Page<HabitResponse> habitsPage;

        // Filtrage selon les paramètres
        if (search != null && !search.trim().isEmpty()) {
            habitsPage = habitService.searchByTitle(currentUserId, search.trim(), pageable);
        } else if (category != null) {
            habitsPage = habitService.findByCategory(currentUserId, category, pageable);
        } else if (Boolean.TRUE.equals(active)) {
            habitsPage = habitService.findActiveByUser(currentUserId, pageable);
        } else {
            habitsPage = habitService.findAllByUser(currentUserId, pageable);
        }

        // Conversion en PagedModel avec HATEOAS
        PagedModel<EntityModel<HabitResponse>> pagedModel = PagedModel.of(
                habitsPage.getContent().stream()
                        .map(habit -> buildHabitModel(habit, authentication))
                        .toList(),
                new PagedModel.PageMetadata(
                        habitsPage.getSize(),
                        habitsPage.getNumber(),
                        habitsPage.getTotalElements(),
                        habitsPage.getTotalPages()
                )
        );

        // Liens de navigation
        pagedModel.add(linkTo(methodOn(HabitController.class)
                .getAllHabits(pageable, search, category, active, authentication))
                .withSelfRel());

        return ResponseEntity.ok(pagedModel);
    }

    @PostMapping
    public ResponseEntity<EntityModel<HabitResponse>> createHabit(
            @Valid @RequestBody HabitCreateRequest request,
            Authentication authentication) {

        Long currentUserId = habitSecurityService.getCurrentUserId(authentication);

        log.info("Création d'une habitude pour l'utilisateur: {}", currentUserId);

        HabitResponse createdHabit = habitService.createHabit(currentUserId, request);
        EntityModel<HabitResponse> habitModel = buildHabitModel(createdHabit, authentication);

        // URI de la ressource créée
        URI location = linkTo(methodOn(HabitController.class)
                .getHabitById(createdHabit.getId(), authentication))
                .toUri();

        return ResponseEntity.created(location).body(habitModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<HabitResponse>> getHabitById(
            @PathVariable Long id,
            Authentication authentication) {

        Long currentUserId = habitSecurityService.getCurrentUserId(authentication);

        log.info("Récupération de l'habitude {} pour l'utilisateur: {}", id, currentUserId);

        HabitResponse habit = habitService.findById(id, currentUserId);
        EntityModel<HabitResponse> habitModel = buildHabitModel(habit, authentication);

        return ResponseEntity.ok(habitModel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<HabitResponse>> updateHabit(
            @PathVariable Long id,
            @Valid @RequestBody HabitUpdateRequest request,
            Authentication authentication) {

        Long currentUserId = habitSecurityService.getCurrentUserId(authentication);

        log.info("Mise à jour de l'habitude {} pour l'utilisateur: {}", id, currentUserId);

        HabitResponse updatedHabit = habitService.updateHabit(id, currentUserId, request);
        EntityModel<HabitResponse> habitModel = buildHabitModel(updatedHabit, authentication);

        return ResponseEntity.ok(habitModel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(
            @PathVariable Long id,
            Authentication authentication) {

        Long currentUserId = habitSecurityService.getCurrentUserId(authentication);

        log.info("Suppression de l'habitude {} pour l'utilisateur: {}", id, currentUserId);

        habitService.deleteHabit(id, currentUserId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<EntityModel<HabitResponse>> toggleHabitStatus(
            @PathVariable Long id,
            Authentication authentication) {

        Long currentUserId = habitSecurityService.getCurrentUserId(authentication);

        log.info("Changement de statut de l'habitude {} pour l'utilisateur: {}", id, currentUserId);

        HabitResponse habit = habitService.toggleHabitStatus(id, currentUserId);
        EntityModel<HabitResponse> habitModel = buildHabitModel(habit, authentication);

        return ResponseEntity.ok(habitModel);
    }

    // Endpoints pour filtrage par catégorie (utile pour le frontend)
    @GetMapping("/categories")
    public ResponseEntity<Habit.Category[]> getAvailableCategories() {
        return ResponseEntity.ok(Habit.Category.values());
    }

    @GetMapping("/frequencies")
    public ResponseEntity<Habit.Frequency[]> getAvailableFrequencies() {
        return ResponseEntity.ok(Habit.Frequency.values());
    }

    // === MÉTHODES PRIVÉES HATEOAS ===

    private EntityModel<HabitResponse> buildHabitModel(HabitResponse habit, Authentication authentication) {
        EntityModel<HabitResponse> habitModel = EntityModel.of(habit);

        // Self link
        habitModel.add(linkTo(methodOn(HabitController.class)
                .getHabitById(habit.getId(), authentication))
                .withSelfRel());

        // Liens de modification (toujours disponibles pour le propriétaire)
        habitModel.add(linkTo(methodOn(HabitController.class)
                .updateHabit(habit.getId(), null, authentication))
                .withRel("update"));

        habitModel.add(linkTo(methodOn(HabitController.class)
                .deleteHabit(habit.getId(), authentication))
                .withRel("delete"));

        habitModel.add(linkTo(methodOn(HabitController.class)
                .toggleHabitStatus(habit.getId(), authentication))
                .withRel("toggle"));

        // Liens vers les ressources liées
        addRelatedResourcesLinks(habitModel, habit);

        return habitModel;
    }

    private void addRelatedResourcesLinks(EntityModel<HabitResponse> habitModel, HabitResponse habit) {
        // Lien vers la liste des habitudes
        habitModel.add(linkTo(methodOn(HabitController.class)
                .getAllHabits(null, null, null, null, null))
                .withRel("habits"));

        // Liens vers les progressions (à implémenter dans ProgressController)
        // habitModel.add(linkTo(methodOn(ProgressController.class)
        //         .getProgressByHabit(habit.getId(), null, null))
        //         .withRel("progress"));

        // Lien vers le propriétaire
        // habitModel.add(linkTo(methodOn(UserController.class)
        //         .getUserById(habit.getUserId(), null))
        //         .withRel("owner"));
    }
}