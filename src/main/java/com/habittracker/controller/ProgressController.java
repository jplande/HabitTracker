package com.habittracker.controller;

import com.habittracker.dto.progress.ProgressCreateRequest;
import com.habittracker.dto.progress.ProgressResponse;
import com.habittracker.dto.progress.ProgressStatsResponse;
import com.habittracker.dto.progress.ProgressUpdateRequest;
import com.habittracker.service.ProgressService;
import com.habittracker.service.UserSecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ProgressController {

    private final ProgressService progressService;
    private final UserSecurityService userSecurityService;

    // ===== PROGRESSION INDIVIDUELLE =====

    @GetMapping("/progress/{id}")
    public ResponseEntity<EntityModel<ProgressResponse>> getProgress(@PathVariable Long id, Authentication auth) {
        ProgressResponse progress = progressService.findById(id);
        userSecurityService.validateUserAccess(progress.getUserId(), auth);

        EntityModel<ProgressResponse> model = EntityModel.of(progress)
                .add(linkTo(methodOn(ProgressController.class).getProgress(id, auth)).withSelfRel())
                .add(linkTo(methodOn(ProgressController.class).updateProgress(id, null, auth)).withRel("update"))
                .add(linkTo(methodOn(ProgressController.class).deleteProgress(id, auth)).withRel("delete"));

        return ResponseEntity.ok(model);
    }

    @PutMapping("/progress/{id}")
    public ResponseEntity<EntityModel<ProgressResponse>> updateProgress(
            @PathVariable Long id, @Valid @RequestBody ProgressUpdateRequest request, Authentication auth) {

        ProgressResponse progress = progressService.updateProgress(id, request, auth);
        EntityModel<ProgressResponse> model = EntityModel.of(progress)
                .add(linkTo(methodOn(ProgressController.class).getProgress(id, auth)).withSelfRel());

        return ResponseEntity.ok(model);
    }

    @DeleteMapping("/progress/{id}")
    public ResponseEntity<Void> deleteProgress(@PathVariable Long id, Authentication auth) {
        progressService.deleteProgress(id, auth);
        return ResponseEntity.noContent().build();
    }

    // ===== PROGRESSION PAR HABITUDE =====

    @GetMapping("/habits/{habitId}/progress")
    public ResponseEntity<PagedModel<EntityModel<ProgressResponse>>> getHabitProgress(
            @PathVariable Long habitId, @PageableDefault(size = 30) Pageable pageable, Authentication auth) {

        Page<ProgressResponse> progressPage = progressService.findHabitProgress(habitId, pageable);

        List<EntityModel<ProgressResponse>> models = progressPage.getContent().stream()
                .map(p -> EntityModel.of(p)
                        .add(linkTo(methodOn(ProgressController.class).getProgress(p.getId(), auth)).withSelfRel()))
                .toList();

        PagedModel<EntityModel<ProgressResponse>> pagedModel = (PagedModel<EntityModel<ProgressResponse>>) PagedModel.of(models,
                        new PagedModel.PageMetadata(progressPage.getSize(), progressPage.getNumber(),
                                progressPage.getTotalElements(), progressPage.getTotalPages()))
                .add(linkTo(methodOn(ProgressController.class).getHabitProgress(habitId, pageable, auth)).withSelfRel());

        return ResponseEntity.ok(pagedModel);
    }

    @PostMapping("/habits/{habitId}/progress")
    public ResponseEntity<EntityModel<ProgressResponse>> createProgress(
            @PathVariable Long habitId, @Valid @RequestBody ProgressCreateRequest request, Authentication auth) {

        ProgressResponse progress = progressService.createProgress(habitId, request, auth);
        EntityModel<ProgressResponse> model = EntityModel.of(progress)
                .add(linkTo(methodOn(ProgressController.class).getProgress(progress.getId(), auth)).withSelfRel());

        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @GetMapping("/habits/{habitId}/progress/stats")
    public ResponseEntity<EntityModel<ProgressStatsResponse>> getHabitStats(
            @PathVariable Long habitId, @RequestParam(defaultValue = "30") int days, Authentication auth) {

        ProgressStatsResponse stats = progressService.getHabitStatistics(habitId, days);
        EntityModel<ProgressStatsResponse> model = EntityModel.of(stats)
                .add(linkTo(methodOn(ProgressController.class).getHabitStats(habitId, days, auth)).withSelfRel())
                .add(linkTo(methodOn(ProgressController.class).getHabitChartData(habitId, days, "line", auth)).withRel("charts"));

        return ResponseEntity.ok(model);
    }

    @GetMapping("/habits/{habitId}/progress/charts")
    public ResponseEntity<Map<String, Object>> getHabitChartData(
            @PathVariable Long habitId,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "line") String chartType,
            Authentication auth) {

        Map<String, Object> chartData = progressService.getChartData(habitId, days, chartType);
        return ResponseEntity.ok(chartData);
    }

    // ===== PROGRESSION PAR UTILISATEUR =====

    @GetMapping("/users/{userId}/progress")
    public ResponseEntity<PagedModel<EntityModel<ProgressResponse>>> getUserProgress(
            @PathVariable Long userId, @PageableDefault(size = 20) Pageable pageable, Authentication auth) {

        userSecurityService.validateUserAccess(userId, auth);
        Page<ProgressResponse> progressPage = progressService.findUserProgress(userId, pageable);

        List<EntityModel<ProgressResponse>> models = progressPage.getContent().stream()
                .map(p -> EntityModel.of(p)
                        .add(linkTo(methodOn(ProgressController.class).getProgress(p.getId(), auth)).withSelfRel()))
                .toList();

        PagedModel<EntityModel<ProgressResponse>> pagedModel = (PagedModel<EntityModel<ProgressResponse>>) PagedModel.of(models,
                        new PagedModel.PageMetadata(progressPage.getSize(), progressPage.getNumber(),
                                progressPage.getTotalElements(), progressPage.getTotalPages()))
                .add(linkTo(methodOn(ProgressController.class).getUserProgress(userId, pageable, auth)).withSelfRel());

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/users/{userId}/progress/today")
    public ResponseEntity<List<EntityModel<ProgressResponse>>> getTodayProgress(
            @PathVariable Long userId, Authentication auth) {

        userSecurityService.validateUserAccess(userId, auth);
        List<ProgressResponse> todayProgress = progressService.findTodayProgress(userId);

        List<EntityModel<ProgressResponse>> models = todayProgress.stream()
                .map(p -> EntityModel.of(p)
                        .add(linkTo(methodOn(ProgressController.class).getProgress(p.getId(), auth)).withSelfRel()))
                .toList();

        return ResponseEntity.ok(models);
    }

    @GetMapping("/users/{userId}/progress/summary")
    public ResponseEntity<Map<String, Object>> getProgressSummary(
            @PathVariable Long userId, @RequestParam(defaultValue = "7") int days, Authentication auth) {

        userSecurityService.validateUserAccess(userId, auth);
        Map<String, Object> summary = progressService.getProgressSummary(userId, days);
        return ResponseEntity.ok(summary);
    }
}