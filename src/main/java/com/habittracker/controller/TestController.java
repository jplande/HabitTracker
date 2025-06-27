// 🔧 SOLUTION 3: Créer un TestController pour debug

package com.habittracker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur de test pour débugger les problèmes JWT
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    /**
     * Test endpoint public (sans auth)
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> testPublic() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Endpoint public fonctionne");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint protégé (avec auth)
     */
    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> testProtected(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication != null) {
            response.put("message", "✅ Authentication OK");
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
            response.put("principal", authentication.getPrincipal().getClass().getSimpleName());
            log.info("🔐 Auth réussie: {}", authentication.getName());
        } else {
            response.put("message", "❌ Pas d'authentication");
            log.warn("🔐 Pas d'authentication dans /test/protected");
        }

        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Test graphique simple sans sécurité
     */
    @GetMapping("/chart-simple")
    public ResponseEntity<Map<String, Object>> testChartSimple() {
        Map<String, Object> chartData = new HashMap<>();

        // Données factices Chart.js
        Map<String, Object> data = new HashMap<>();
        data.put("labels", new String[]{"Jour 1", "Jour 2", "Jour 3", "Jour 4", "Jour 5"});

        Map<String, Object> dataset = new HashMap<>();
        dataset.put("label", "Test Data");
        dataset.put("data", new double[]{10, 20, 15, 25, 30});
        dataset.put("backgroundColor", "#3B82F6");

        data.put("datasets", new Object[]{dataset});

        chartData.put("type", "bar");
        chartData.put("data", data);
        chartData.put("message", "✅ Graphique test généré");

        log.info("📊 Graphique test généré");
        return ResponseEntity.ok(chartData);
    }

    /**
     * Test de décodage JWT
     */
    @GetMapping("/jwt-info")
    public ResponseEntity<Map<String, Object>> testJwtInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        response.put("authHeader", authHeader != null ? "Present" : "Missing");
        response.put("authHeaderValue", authHeader);

        if (authentication != null) {
            response.put("authPresent", true);
            response.put("authName", authentication.getName());
            response.put("authType", authentication.getClass().getSimpleName());
            response.put("authorities", authentication.getAuthorities());

            // Détails du principal
            Object principal = authentication.getPrincipal();
            response.put("principalType", principal.getClass().getSimpleName());
            response.put("principalString", principal.toString());

        } else {
            response.put("authPresent", false);
        }

        log.info("🔍 JWT Info - Header: {}, Auth: {}",
                authHeader != null ? "Present" : "Missing",
                authentication != null ? "Present" : "Missing");

        return ResponseEntity.ok(response);
    }
}