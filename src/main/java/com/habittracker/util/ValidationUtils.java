package com.habittracker.util;

import com.habittracker.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    /**
     * Valide qu'une chaîne n'est pas nulle ou vide
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(fieldName + " ne peut pas être vide");
        }
    }

    /**
     * Valide qu'un objet n'est pas null
     */
    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new BusinessException(fieldName + " ne peut pas être null");
        }
    }

    /**
     * Valide qu'un nombre est positif
     */
    public static void validatePositive(Number value, String fieldName) {
        validateNotNull(value, fieldName);
        if (value.doubleValue() <= 0) {
            throw new BusinessException(fieldName + " doit être positif");
        }
    }

    /**
     * Valide qu'un ID est valide (positif et non null)
     */
    public static void validateId(Long id, String entityName) {
        validateNotNull(id, "ID de " + entityName);
        if (id <= 0) {
            throw new BusinessException("ID de " + entityName + " doit être positif");
        }
    }

    /**
     * Valide le format d'un email
     */
    public static void validateEmail(String email) {
        validateNotEmpty(email, "Email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("Format d'email invalide");
        }
    }

    /**
     * Valide le format d'un nom d'utilisateur
     */
    public static void validateUsername(String username) {
        validateNotEmpty(username, "Nom d'utilisateur");
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new BusinessException("Le nom d'utilisateur doit contenir entre 3 et 20 caractères (lettres, chiffres, underscore uniquement)");
        }
    }

    /**
     * Valide qu'une chaîne respecte une longueur minimale et maximale
     */
    public static void validateLength(String value, String fieldName, int minLength, int maxLength) {
        validateNotEmpty(value, fieldName);
        if (value.length() < minLength || value.length() > maxLength) {
            throw new BusinessException(String.format("%s doit contenir entre %d et %d caractères",
                    fieldName, minLength, maxLength));
        }
    }

    /**
     * Valide qu'une valeur est dans une plage donnée
     */
    public static void validateRange(double value, String fieldName, double min, double max) {
        if (value < min || value > max) {
            throw new BusinessException(String.format("%s doit être entre %.2f et %.2f",
                    fieldName, min, max));
        }
    }
}