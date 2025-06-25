package com.habittracker.controller;

import com.habittracker.dto.UserResponse;
import com.habittracker.dto.UserUpdateRequest;
import com.habittracker.service.UserSecurityService;
import com.habittracker.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserSecurityService userSecurityService;

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponse>> getUserById(
            @PathVariable Long id,
            Authentication authentication) {

        userSecurityService.validateUserAccess(id, authentication);

        log.info("Récupération utilisateur ID: {}", id);
        UserResponse user = userService.findById(id);
        EntityModel<UserResponse> userModel = buildUserModel(user, authentication);

        return ResponseEntity.ok(userModel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {

        userSecurityService.validateUserAccess(id, authentication);

        log.info("Mise à jour utilisateur ID: {}", id);
        UserResponse updatedUser = userService.updateUser(id, request);
        EntityModel<UserResponse> userModel = buildUserModel(updatedUser, authentication);

        return ResponseEntity.ok(userModel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {

        userSecurityService.validateUserAccess(id, authentication);

        log.info("Suppression utilisateur ID: {}", id);
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    private EntityModel<UserResponse> buildUserModel(UserResponse user, Authentication authentication) {
        EntityModel<UserResponse> userModel = EntityModel.of(user);

        // Self link
        userModel.add(linkTo(methodOn(UserController.class)
                .getUserById(user.getId(), authentication))
                .withSelfRel());

        // Conditional links based on permissions
        if (userSecurityService.canModifyUser(user.getId(), authentication)) {
            userModel.add(linkTo(methodOn(UserController.class)
                    .updateUser(user.getId(), null, authentication))
                    .withRel("update"));

            userModel.add(linkTo(methodOn(UserController.class)
                    .deleteUser(user.getId(), authentication))
                    .withRel("delete"));
        }

        // Related resources links
        addRelatedResourcesLinks(userModel, user.getId());

        return userModel;
    }

    private void addRelatedResourcesLinks(EntityModel<UserResponse> userModel, Long userId) {
        // Note: Ces contrôleurs seront créés plus tard dans le jeu comme le dit MAAARRRCCCCCC
        // userModel.add(linkTo(HabitController.class).slash(userId).withRel("habits"));
        // userModel.add(linkTo(ProgressController.class).slash(userId).withRel("progress"));
        // userModel.add(linkTo(AchievementController.class).slash(userId).withRel("achievements"));
    }
}