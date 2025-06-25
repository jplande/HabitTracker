package com.habittracker.controller;

import com.habittracker.dto.achievement.*;
import com.habittracker.service.AchievementService;
import com.habittracker.service.UserSecurityService;
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
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AchievementController {

    private final AchievementService achievementService;
    private final UserSecurityService userSecurityService;

    // ===== BADGES UTILISATEUR =====

    @GetMapping("/users/{userId}/achievements")
    public ResponseEntity<PagedModel<EntityModel<AchievementResponse>>> getUserAchievements(
            @PathVariable Long userId, @PageableDefault(size = 20) Pageable pageable, Authentication auth) {

        userSecurityService.validateUserAccess(userId, auth);
        Page<AchievementResponse> achievementPage = achievementService.findUserAchievements(userId, pageable);

        List<EntityModel<AchievementResponse>> models = achievementPage.getContent().stream()
                .map(achievement -> EntityModel.of(achievement)
                        .add(linkTo(methodOn(AchievementController.class)
                                .getUserAchievements(userId, pageable, auth)).withSelfRel()))
                .toList();

        PagedModel<EntityModel<AchievementResponse>> pagedModel = (PagedModel<EntityModel<AchievementResponse>>) PagedModel.of(models,
                        new PagedModel.PageMetadata(achievementPage.getSize(), achievementPage.getNumber(),
                                achievementPage.getTotalElements(), achievementPage.getTotalPages()))
                .add(linkTo(methodOn(AchievementController.class)
                        .getUserAchievements(userId, pageable, auth)).withSelfRel())
                .add(linkTo(methodOn(AchievementController.class)
                        .getUserSummary(userId, auth)).withRel("summary"));

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/users/{userId}/achievements/recent")
    public ResponseEntity<List<EntityModel<AchievementResponse>>> getRecentAchievements(
            @PathVariable Long userId, @RequestParam(defaultValue = "7") int days, Authentication auth) {

        userSecurityService.validateUserAccess(userId, auth);
        List<AchievementResponse> recent = achievementService.findRecentAchievements(userId, days);

        List<EntityModel<AchievementResponse>> models = recent.stream()
                .map(achievement -> EntityModel.of(achievement)
                        .add(linkTo(methodOn(AchievementController.class)
                                .getRecentAchievements(userId, days, auth)).withSelfRel()))
                .toList();

        return ResponseEntity.ok(models);
    }

    @GetMapping("/users/{userId}/achievements/summary")
    public ResponseEntity<EntityModel<AchievementSummaryResponse>> getUserSummary(
            @PathVariable Long userId, Authentication auth) {

        userSecurityService.validateUserAccess(userId, auth);
        AchievementSummaryResponse summary = achievementService.getUserAchievementSummary(userId);

        EntityModel<AchievementSummaryResponse> model = EntityModel.of(summary)
                .add(linkTo(methodOn(AchievementController.class).getUserSummary(userId, auth)).withSelfRel())
                .add(linkTo(methodOn(AchievementController.class).getUserAchievements(userId, null, auth)).withRel("achievements"))
                .add(linkTo(methodOn(AchievementController.class).checkAchievements(null, auth)).withRel("check"));

        return ResponseEntity.ok(model);
    }

    // ===== VÉRIFICATION BADGES =====

    @PostMapping("/achievements/check")
    public ResponseEntity<EntityModel<AchievementCheckResponse>> checkAchievements(
            @Valid @RequestBody AchievementCheckRequest request, Authentication auth) {

        userSecurityService.validateUserAccess(request.getUserId(), auth);
        AchievementCheckResponse response = achievementService.checkAndUnlockAchievements(request);

        EntityModel<AchievementCheckResponse> model = EntityModel.of(response)
                .add(linkTo(methodOn(AchievementController.class).checkAchievements(request, auth)).withSelfRel())
                .add(linkTo(methodOn(AchievementController.class)
                        .getUserAchievements(request.getUserId(), null, auth)).withRel("achievements"));

        return ResponseEntity.ok(model);
    }

    // ===== UTILITAIRES =====

    @GetMapping("/achievements/types")
    public ResponseEntity<List<String>> getTypes() {
        List<String> types = List.of("CONSISTENCY", "MILESTONE", "STREAK", "DEDICATION",
                "OVERACHIEVER", "VARIETY", "EARLY_BIRD", "PERSEVERANCE");
        return ResponseEntity.ok(types);
    }
}