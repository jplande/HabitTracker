package com.habittracker.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller ultra simple pour test
 */
@RestController
@RequestMapping("/api/simple")
@Slf4j
public class SimpleTestController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        log.info("🏓 PING reçu");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "PONG");
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "OK");
        return response;
    }

    @GetMapping("/chart-fake")
    public Map<String, Object> fakechart() {
        log.info("📊 Chart fake demandé");
        Map<String, Object> chart = new HashMap<>();
        chart.put("type", "line");
        chart.put("message", "Graphique factice");
        chart.put("data", Map.of(
                "labels", new String[]{"Lun", "Mar", "Mer", "Jeu", "Ven"},
                "datasets", new Object[]{
                        Map.of(
                                "label", "Test Data",
                                "data", new int[]{10, 20, 15, 25, 30},
                                "backgroundColor", "#3B82F6"
                        )
                }
        ));
        return chart;
    }

    @GetMapping("/stats-fake")
    public Map<String, Object> fakestats() {
        log.info("📈 Stats fake demandées");
        return Map.of(
                "totalHabits", 5,
                "activeHabits", 3,
                "currentStreak", 7,
                "completionRate", 85.5,
                "message", "Statistiques factices"
        );
    }
}