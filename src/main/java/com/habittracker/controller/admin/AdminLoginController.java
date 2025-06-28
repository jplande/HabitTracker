package com.habittracker.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Contrôleur pour la gestion de l'authentification admin
 */
@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminLoginController {

    /**
     * Page de connexion admin
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
            log.warn("Tentative de connexion admin échouée");
        }

        if (logout != null) {
            model.addAttribute("message", "Vous avez été déconnecté avec succès");
            log.info("Déconnexion admin réussie");
        }

        model.addAttribute("pageTitle", "Connexion Admin");
        return "admin/login";
    }

    /**
     * Redirection racine admin vers dashboard
     */
    @GetMapping({"", "/"})
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    /**
     * Page d'accès refusé
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("pageTitle", "Accès Refusé");
        return "admin/access-denied";
    }
}