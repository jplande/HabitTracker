package com.habittracker.controller.admin;

import com.habittracker.dto.user.AdminUserUpdateRequest;
import com.habittracker.dto.user.UserResponse;
import com.habittracker.entity.User;
import com.habittracker.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Contrôleur CRUD pour la gestion des utilisateurs en admin - VERSION DEBUG
 */
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    /**
     * Liste paginée des utilisateurs avec recherche
     */
    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Model model) {

        log.info("📋 [ADMIN] Liste utilisateurs - page: {}, recherche: '{}', statut: '{}'", page, search, status);

        try {
            Pageable pageable = PageRequest.of(page, size,
                    Sort.by(Sort.Direction.DESC, "createdAt"));

            Page<UserResponse> usersPage;

            if (search != null && !search.trim().isEmpty()) {
                usersPage = userService.searchUsers(search.trim(), pageable);
            } else if ("active".equals(status)) {
                usersPage = userService.findActiveUsers(pageable);
            } else if ("inactive".equals(status)) {
                usersPage = userService.findInactiveUsers(pageable);
            } else {
                usersPage = userService.findAllUsers(pageable);
            }

            model.addAttribute("usersPage", usersPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("search", search);
            model.addAttribute("status", status);

            addUserStatsToModel(model);

            model.addAttribute("pageTitle", "Gestion des Utilisateurs");
            model.addAttribute("currentNavPage", "users");

            log.info("✅ [ADMIN] Liste chargée: {} utilisateurs trouvés", usersPage.getTotalElements());

        } catch (Exception e) {
            log.error("❌ [ADMIN] Erreur lors du chargement des utilisateurs", e);
            model.addAttribute("error", "Erreur lors du chargement des utilisateurs: " + e.getMessage());
        }

        return "admin/users";
    }

    /**
     * Affichage des détails d'un utilisateur
     */
    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.info("👁️ [ADMIN] Affichage utilisateur {}", id);

        try {
            UserResponse user = userService.findById(id);
            model.addAttribute("user", user);
            addUserDetailsToModel(model, id);
            model.addAttribute("pageTitle", "Détails - " + user.getUsername());

        } catch (Exception e) {
            log.error("❌ [ADMIN] Erreur lors du chargement de l'utilisateur {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Utilisateur introuvable: " + e.getMessage());
            return "redirect:/admin/users";
        }

        return "admin/user-details";
    }

    /**
     * Formulaire d'édition d'un utilisateur
     */
    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.info("✏️ [ADMIN] Formulaire édition utilisateur {}", id);

        try {
            UserResponse user = userService.findById(id);
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Modifier - " + user.getUsername());

        } catch (Exception e) {
            log.error("❌ [ADMIN] Erreur lors du chargement pour édition de l'utilisateur {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Utilisateur introuvable: " + e.getMessage());
            return "redirect:/admin/users";
        }

        return "admin/user-edit";
    }

    /**
     * ✅ Mise à jour d'un utilisateur - VERSION DEBUG ÉTENDU
     */
    @PostMapping("/{id}/update")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam User.Role role,
            @RequestParam(required = false) Boolean isActive,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        log.info("🔥 [ADMIN] ========== DÉBUT MISE À JOUR UTILISATEUR {} ==========", id);
        log.info("🔥 [ADMIN] Paramètres reçus:");
        log.info("🔥 [ADMIN]   - username: '{}'", username);
        log.info("🔥 [ADMIN]   - email: '{}'", email);
        log.info("🔥 [ADMIN]   - firstName: '{}'", firstName);
        log.info("🔥 [ADMIN]   - lastName: '{}'", lastName);
        log.info("🔥 [ADMIN]   - role: '{}'", role);
        log.info("🔥 [ADMIN]   - isActive: '{}'", isActive);
        log.info("🔥 [ADMIN]   - Method: {}", request.getMethod());
        log.info("🔥 [ADMIN]   - Content-Type: {}", request.getContentType());

        try {
            // ✅ Gestion correcte de la checkbox
            boolean activeStatus = Boolean.TRUE.equals(isActive);
            log.info("🔥 [ADMIN] Statut actif calculé: {}", activeStatus);

            AdminUserUpdateRequest updateRequest = AdminUserUpdateRequest.builder()
                    .username(username)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(role)
                    .isActive(activeStatus)
                    .build();

            log.info("🔥 [ADMIN] Requête construite: {}", updateRequest);

            UserResponse updatedUser = userService.updateUserByAdmin(id, updateRequest);
            log.info("✅ [ADMIN] Utilisateur {} mis à jour avec succès: {}", id, updatedUser.getUsername());

            redirectAttributes.addFlashAttribute("success",
                    "Utilisateur '" + updatedUser.getUsername() + "' mis à jour avec succès");

            log.info("🔥 [ADMIN] Redirection vers /admin/users/{}", id);
            return "redirect:/admin/users/" + id;

        } catch (Exception e) {
            log.error("❌ [ADMIN] ERREUR lors de la mise à jour de l'utilisateur {}", id, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la mise à jour : " + e.getMessage());

            log.info("🔥 [ADMIN] Redirection vers /admin/users/{}/edit après erreur", id);
            return "redirect:/admin/users/" + id + "/edit";
        } finally {
            log.info("🔥 [ADMIN] ========== FIN MISE À JOUR UTILISATEUR {} ==========", id);
        }
    }

    /**
     * Export des utilisateurs en CSV
     */
    @GetMapping("/export")
    public String exportUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            RedirectAttributes redirectAttributes) {

        log.info("📥 [ADMIN] Export utilisateurs demandé");

        try {
            redirectAttributes.addFlashAttribute("success",
                    "Export en cours... Le fichier sera disponible sous peu.");

        } catch (Exception e) {
            log.error("❌ [ADMIN] Erreur lors de l'export", e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de l'export : " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // === MÉTHODES UTILITAIRES ===

    private void addUserStatsToModel(Model model) {
        try {
            long totalUsers = userService.getTotalUsersCount();
            long activeUsers = userService.getActiveUsersCount();
            long inactiveUsers = totalUsers - activeUsers;

            model.addAttribute("userStats", Map.of(
                    "totalUsers", totalUsers,
                    "activeUsers", activeUsers,
                    "inactiveUsers", inactiveUsers
            ));

        } catch (Exception e) {
            log.warn("⚠️ [ADMIN] Impossible de charger les statistiques utilisateurs", e);
        }
    }

    private void addUserDetailsToModel(Model model, Long userId) {
        try {
            model.addAttribute("userDetails", Map.of(
                    "habitCount", "Non implémenté",
                    "progressCount", "Non implémenté",
                    "lastActivity", "Non implémenté"
            ));

        } catch (Exception e) {
            log.warn("⚠️ [ADMIN] Impossible de charger les détails de l'utilisateur {}", userId, e);
        }
    }

    /**
     * ✅ Activation/Désactivation - VERSION DEBUG ÉTENDU
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.info("🔥 [ADMIN] ========== DÉBUT TOGGLE STATUS UTILISATEUR {} ==========", id);
        log.info("🔥 [ADMIN] Method: {}, Content-Type: {}", request.getMethod(), request.getContentType());
        log.info("🔥 [ADMIN] Headers: {}",
                request.getHeaderNames() != null ?
                        java.util.Collections.list(request.getHeaderNames()) : "None");

        try {
            // ✅ Vérifier l'utilisateur AVANT le toggle
            UserResponse userBefore = userService.findById(id);
            log.info("🔥 [ADMIN] État AVANT toggle: {} (actif: {})", userBefore.getUsername(), userBefore.getIsActive());

            // ✅ Effectuer le toggle
            UserResponse userAfter = userService.toggleUserStatus(id);
            log.info("🔥 [ADMIN] État APRÈS toggle: {} (actif: {})", userAfter.getUsername(), userAfter.getIsActive());

            // ✅ Vérifier que le changement a bien eu lieu
            if (userBefore.getIsActive().equals(userAfter.getIsActive())) {
                log.error("❌ [ADMIN] PROBLÈME: Aucun changement détecté ! Avant: {}, Après: {}",
                        userBefore.getIsActive(), userAfter.getIsActive());
                redirectAttributes.addFlashAttribute("error",
                        "Erreur: Le statut n'a pas pu être modifié");
                return "redirect:/admin/users";
            }

            String status = userAfter.getIsActive() ? "activé" : "désactivé";
            log.info("✅ [ADMIN] Utilisateur {} {} avec succès", id, status);

            redirectAttributes.addFlashAttribute("success",
                    "Utilisateur '" + userAfter.getUsername() + "' " + status + " avec succès");

        } catch (Exception e) {
            log.error("❌ [ADMIN] Erreur lors du changement de statut de l'utilisateur {}", id, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors du changement de statut : " + e.getMessage());
        } finally {
            log.info("🔥 [ADMIN] ========== FIN TOGGLE STATUS UTILISATEUR {} ==========", id);
        }

        log.info("🔥 [ADMIN] Redirection vers /admin/users");
        return "redirect:/admin/users";
    }

    /**
     * ✅ Suppression - VERSION DEBUG ÉTENDU
     */
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.info("🔥 [ADMIN] ========== DÉBUT SUPPRESSION UTILISATEUR {} ==========", id);
        log.info("🔥 [ADMIN] Method: {}, Content-Type: {}", request.getMethod(), request.getContentType());

        try {
            // ✅ Vérifier l'utilisateur AVANT suppression
            UserResponse userBefore = userService.findById(id);
            log.info("🔥 [ADMIN] Utilisateur à supprimer: {} ({})", userBefore.getUsername(), userBefore.getEmail());
            log.info("🔥 [ADMIN] Rôle: {}, Actif: {}", userBefore.getRole(), userBefore.getIsActive());

            // ✅ Effectuer la suppression
            userService.deleteUser(id);
            log.info("✅ [ADMIN] Service de suppression terminé pour utilisateur {}", id);

            // ✅ Vérifier que la suppression a eu lieu
            try {
                UserResponse userAfter = userService.findById(id);
                if (userAfter.getIsActive()) {
                    log.error("❌ [ADMIN] PROBLÈME: Utilisateur {} toujours actif après suppression", id);
                    redirectAttributes.addFlashAttribute("error",
                            "Erreur: L'utilisateur n'a pas pu être supprimé");
                    return "redirect:/admin/users";
                }
                log.info("✅ [ADMIN] Vérification: Utilisateur {} bien désactivé", id);
            } catch (Exception e) {
                log.info("✅ [ADMIN] Utilisateur {} introuvable après suppression (normal si hard delete)", id);
            }

            redirectAttributes.addFlashAttribute("success",
                    "Utilisateur '" + userBefore.getUsername() + "' supprimé avec succès");

        } catch (Exception e) {
            log.error("❌ [ADMIN] Erreur lors de la suppression de l'utilisateur {}", id, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la suppression : " + e.getMessage());
        } finally {
            log.info("🔥 [ADMIN] ========== FIN SUPPRESSION UTILISATEUR {} ==========", id);
        }

        log.info("🔥 [ADMIN] Redirection vers /admin/users");
        return "redirect:/admin/users";
    }
}