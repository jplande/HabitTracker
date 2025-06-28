package com.habittracker.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Gère les ressources statiques manquantes (favicon, .well-known, etc.)
     * Ignoré pour réduire le spam dans les logs
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, WebRequest request) {
        String path = getPath(request);

        // ✅ Ignorer silencieusement les requêtes de ressources système communes
        if (isSystemResource(path)) {
            log.debug("🔇 Ressource système ignorée: {}", path);
            return ResponseEntity.notFound().build();
        }

        log.warn("🔍 Ressource non trouvée: {}", path);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Ressource non trouvée")
                .message("La ressource demandée n'existe pas")
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Gère les erreurs de validation des champs
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Erreur de validation: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Erreur de validation")
                .message("Les données fournies ne sont pas valides")
                .path(getPath(request))
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Gère les erreurs de binding (formulaires)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, WebRequest request) {
        log.warn("Erreur de binding: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Erreur de données")
                .message("Les données du formulaire ne sont pas valides")
                .path(getPath(request))
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Gère les erreurs d'authentification
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("Erreur d'authentification: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Non autorisé")
                .message("Identifiants invalides")
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Gère les erreurs d'autorisation (accès refusé)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.warn("Accès refusé: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Accès interdit")
                .message("Vous n'avez pas les permissions nécessaires pour accéder à cette ressource")
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Gère les erreurs de ressource non trouvée
     */
    @ExceptionHandler({NoSuchElementException.class, ResourceNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(Exception ex, WebRequest request) {
        log.warn("Ressource non trouvée: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Ressource non trouvée")
                .message(ex.getMessage())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Gère les erreurs de conflit (données déjà existantes)
     */
    @ExceptionHandler({DataIntegrityViolationException.class, BusinessException.class})
    public ResponseEntity<ErrorResponse> handleConflictException(Exception ex, WebRequest request) {
        log.warn("Conflit de données: {}", ex.getMessage());

        String message = "Une ressource avec ces données existe déjà";
        if (ex instanceof BusinessException) {
            message = ex.getMessage();
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflit")
                .message(message)
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Gère les erreurs de type d'argument incorrect
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Erreur de type d'argument: {}", ex.getMessage());

        String message = String.format("Le paramètre '%s' doit être de type %s",
                ex.getName(),
                ex.getRequiredType().getSimpleName());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Paramètre invalide")
                .message(message)
                .path(getPath(request))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Gère toutes les autres erreurs non prévues
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        String path = getPath(request);

        // ✅ Réduire le spam pour les erreurs système communes
        if (isSystemResource(path)) {
            log.debug("🔇 Erreur système ignorée sur: {}", path);
            return ResponseEntity.notFound().build();
        }

        log.error("Erreur inattendue sur {}: ", path, ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Erreur interne du serveur")
                .message("Une erreur inattendue s'est produite")
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Extrait le chemin de la requête
     */
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    /**
     * Détermine si une ressource est une ressource système à ignorer
     */
    private boolean isSystemResource(String path) {
        return path != null && (
                path.contains("/favicon.ico") ||
                        path.contains("/.well-known/") ||
                        path.contains("/robots.txt") ||
                        path.contains("/sitemap.xml") ||
                        path.contains("/apple-touch-icon") ||
                        path.contains("/browserconfig.xml") ||
                        path.contains("/manifest.json")
        );
    }
}