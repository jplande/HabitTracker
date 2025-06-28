package com.habittracker.controller.admin;

import com.habittracker.exception.BusinessException;
import com.habittracker.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Gestionnaire d'erreurs spécifique pour les contrôleurs d'administration
 */
@ControllerAdvice(basePackages = "com.habittracker.controller.admin")
@Slf4j
public class AdminErrorHandler {

    /**
     * Gère les erreurs de ressource non trouvée dans l'admin
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex,
                                         RedirectAttributes redirectAttributes) {
        log.warn("🔍 Ressource non trouvée dans l'admin: {}", ex.getMessage());

        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/admin/dashboard";
    }

    /**
     * Gère les erreurs métier dans l'admin
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex,
                                          RedirectAttributes redirectAttributes) {
        log.warn("⚠️ Erreur métier dans l'admin: {}", ex.getMessage());

        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/admin/dashboard";
    }

    /**
     * Gère les erreurs génériques dans l'admin
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        log.error("❌ Erreur inattendue dans l'admin", ex);

        model.addAttribute("error", "Une erreur inattendue s'est produite. Veuillez réessayer.");
        model.addAttribute("pageTitle", "Erreur");

        return "admin/error";
    }
}