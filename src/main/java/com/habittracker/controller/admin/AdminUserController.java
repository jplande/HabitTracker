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

import java.util.Map;

/**
 * Contrôleur CRUD pour la gestion des utilisateurs en admin
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
            @RequestParam(required = false) String status, // active, inactive, all
            Model model) {

        log.info("📋 Liste utilisateurs - page: {}, recherche: '{}', statut: '{}'", page, search, status);

        try {
            // Configuration pagination avec tri par date de création
            Pageable pageable = PageRequest.of(page, size,
                    Sort.by(Sort.Direction.DESC, "createdAt"));

            Page<UserResponse> usersPage;

            // Filtrage selon les paramètres
            if (search != null && !search.trim().isEmpty()) {
                usersPage = userService.searchUsers(search.trim(), pageable);
            } else if ("active".equals(status)) {
                usersPage = userService.findActiveUsers(pageable);
            } else if ("inactive".equals(status)) {
                usersPage = userService.findInactiveUsers(pageable);
            } else {
                usersPage = userService.findAllUsers(pageable);
            }

            // Ajout des données au modèle
            model.addAttribute("usersPage", usersPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("search", search);
            model.addAttribute("status", status);

            // Statistiques utilisateurs
            addUserStatsToModel(model);

            model.addAttribute("pageTitle", "Gestion des Utilisateurs");
            model.addAttribute("currentNavPage", "users");

        } catch (Exception e) {
            log.error("❌ Erreur lors du chargement des utilisateurs", e);
            model.addAttribute("error", "Erreur lors du chargement des utilisateurs");
        }

        return "admin/users";
    }

    /**
     * Affichage des détails d'un utilisateur
     */
    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        log.info("👁️ Affichage utilisateur {}", id);

        try {
            UserResponse user = userService.findById(id);
            model.addAttribute("user", user);

            // Statistiques de l'utilisateur
            addUserDetailsToModel(model, id);

            model.addAttribute("pageTitle", "Détails - " + user.getUsername());

        } catch (Exception e) {
            log.error("❌ Erreur lors du chargement de l'utilisateur {}", id, e);
            model.addAttribute("error", "Utilisateur introuvable");
            return "redirect:/admin/users";
        }

        return "admin/user-details";
    }

    /**
     * Formulaire d'édition d'un utilisateur
     */
    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        log.info("✏️ Formulaire édition utilisateur {}", id);

        try {
            UserResponse user = userService.findById(id);
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Modifier - " + user.getUsername());

        } catch (Exception e) {
            log.error("❌ Erreur lors du chargement pour édition de l'utilisateur {}", id, e);
            model.addAttribute("error", "Utilisateur introuvable");
            return "redirect:/admin/users";
        }

        return "admin/user-edit";
    }

    /**
     * Mise à jour d'un utilisateur
     */
    @PostMapping("/{id}/update")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam User.Role role,
            @RequestParam(required = false, defaultValue = "false") Boolean isActive,
            RedirectAttributes redirectAttributes) {

        log.info("💾 Mise à jour utilisateur {} via formulaire", id);

        try {
            // Construction du DTO admin
            AdminUserUpdateRequest request = AdminUserUpdateRequest.builder()
                    .username(username)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(role)
                    .isActive(isActive)
                    .build();

            UserResponse updatedUser = userService.updateUserByAdmin(id, request);
            log.info("✅ Utilisateur {} mis à jour avec succès", id);

            redirectAttributes.addFlashAttribute("success",
                    "Utilisateur '" + updatedUser.getUsername() + "' mis à jour avec succès");

            return "redirect:/admin/users/" + id;

        } catch (Exception e) {
            log.error("❌ Erreur lors de la mise à jour de l'utilisateur {}", id, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la mise à jour : " + e.getMessage());

            return "redirect:/admin/users/" + id + "/edit";
        }
    }

    /**
     * Activation/Désactivation d'un utilisateur
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("🔄 Changement de statut utilisateur {}", id);

        try {
            UserResponse user = userService.toggleUserStatus(id);
            String status = user.getIsActive() ? "activé" : "désactivé";

            log.info("✅ Utilisateur {} {} avec succès", id, status);
            redirectAttributes.addFlashAttribute("success",
                    "Utilisateur '" + user.getUsername() + "' " + status + " avec succès");

        } catch (Exception e) {
            log.error("❌ Erreur lors du changement de statut de l'utilisateur {}", id, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors du changement de statut : " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    /**
     * Suppression d'un utilisateur (avec confirmation)
     */
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("🗑️ Suppression utilisateur {}", id);

        try {
            UserResponse user = userService.findById(id);
            String username = user.getUsername();

            userService.deleteUser(id);

            log.info("✅ Utilisateur {} supprimé avec succès", id);
            redirectAttributes.addFlashAttribute("success",
                    "Utilisateur '" + username + "' supprimé avec succès");

        } catch (Exception e) {
            log.error("❌ Erreur lors de la suppression de l'utilisateur {}", id, e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la suppression : " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    /**
     * Export des utilisateurs en CSV
     */
    @GetMapping("/export")
    public String exportUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            RedirectAttributes redirectAttributes) {

        log.info("📥 Export utilisateurs demandé");

        try {
            // Ici tu peux appeler un service d'export
            // String filePath = userService.exportToCsv(search, status);

            redirectAttributes.addFlashAttribute("success",
                    "Export en cours... Le fichier sera disponible sous peu.");

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'export", e);
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de l'export : " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // === MÉTHODES UTILITAIRES ===

    /**
     * Ajoute les statistiques utilisateurs au modèle
     */
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
            log.warn("⚠️ Impossible de charger les statistiques utilisateurs", e);
        }
    }

    /**
     * Ajoute les détails complémentaires d'un utilisateur
     */
    private void addUserDetailsToModel(Model model, Long userId) {
        try {
            // Tu peux ajouter ici des statistiques spécifiques à l'utilisateur
            // Par exemple : nombre d'habitudes, progressions récentes, etc.

            model.addAttribute("userDetails", Map.of(
                    "habitCount", "Non implémenté",
                    "progressCount", "Non implémenté",
                    "lastActivity", "Non implémenté"
            ));

        } catch (Exception e) {
            log.warn("⚠️ Impossible de charger les détails de l'utilisateur {}", userId, e);
        }
    }
}