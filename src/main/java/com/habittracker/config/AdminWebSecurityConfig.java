package com.habittracker.config;

import com.habittracker.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Configuration de sécurité pour les controllers MVC d'administration - VERSION DEBUG
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class AdminWebSecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Configuration de sécurité pour les routes d'administration MVC
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        log.info("🔧 [SECURITY] Configuration du filtre de sécurité Admin");

        http
                // Appliquer cette config uniquement aux routes /admin/**
                .securityMatcher("/admin/**")

                // Configuration des autorisations
                .authorizeHttpRequests(authz -> {
                    log.info("🔧 [SECURITY] Configuration des autorisations Admin");
                    authz
                            .requestMatchers("/admin/login", "/admin/access-denied").permitAll()
                            .requestMatchers("/admin/**").hasRole("ADMIN")
                            .anyRequest().authenticated();
                })

                // Configuration de l'authentification par formulaire pour MVC
                .formLogin(form -> {
                    log.info("🔧 [SECURITY] Configuration FormLogin Admin");
                    form
                            .loginPage("/admin/login")
                            .loginProcessingUrl("/admin/login")
                            .defaultSuccessUrl("/admin/dashboard", true)
                            .failureUrl("/admin/login?error=true")
                            .usernameParameter("username")
                            .passwordParameter("password")
                            .successHandler(adminSuccessHandler())
                            .failureHandler(adminFailureHandler())
                            .permitAll();
                })

                // Configuration de la déconnexion
                .logout(logout -> {
                    log.info("🔧 [SECURITY] Configuration Logout Admin");
                    logout
                            .logoutUrl("/admin/logout")
                            .logoutSuccessUrl("/admin/login?logout=true")
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID", "HABIT_ADMIN_SESSION")
                            .permitAll();
                })

                // Gestion des erreurs d'accès
                .exceptionHandling(ex -> {
                    log.info("🔧 [SECURITY] Configuration gestion des exceptions Admin");
                    ex.accessDeniedPage("/admin/access-denied");
                })

                // Configuration des sessions pour l'admin
                .sessionManagement(session -> {
                    log.info("🔧 [SECURITY] Configuration sessions Admin");
                    session
                            .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                            .maximumSessions(2)
                            .maxSessionsPreventsLogin(false);
                })

                // ✅ Activer CSRF pour les formulaires MVC mais pas pour l'API
                .csrf(csrf -> {
                    log.info("🔧 [SECURITY] Configuration CSRF Admin - Activé pour MVC");
                    csrf.ignoringRequestMatchers("/api/**");
                });

        log.info("✅ [SECURITY] Configuration Admin terminée");
        return http.build();
    }

    /**
     * Configuration pour les ressources statiques
     */
    @Bean
    @Order(0)
    public SecurityFilterChain staticResourcesFilterChain(HttpSecurity http) throws Exception {
        log.info("🔧 [SECURITY] Configuration ressources statiques");

        http
                .securityMatcher("/css/**", "/js/**", "/images/**", "/favicon.ico", "/webjars/**")
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());

        log.info("✅ [SECURITY] Configuration ressources statiques terminée");
        return http.build();
    }

    /**
     * Provider d'authentification pour les formulaires MVC
     */
    @Bean
    public DaoAuthenticationProvider adminAuthenticationProvider() {
        log.info("🔧 [SECURITY] Configuration DaoAuthenticationProvider Admin");

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        log.info("✅ [SECURITY] DaoAuthenticationProvider Admin configuré");
        return provider;
    }

    /**
     * ✅ Handler de succès avec logs détaillés
     */
    @Bean
    public AuthenticationSuccessHandler adminSuccessHandler() {
        return (request, response, authentication) -> {
            log.info("🎉 [ADMIN] Connexion réussie pour: {}", authentication.getName());
            log.info("🎉 [ADMIN] Rôles: {}", authentication.getAuthorities());
            log.info("🎉 [ADMIN] IP: {}", getClientIP(request));
            log.info("🎉 [ADMIN] Redirection vers: /admin/dashboard");

            response.sendRedirect("/admin/dashboard");
        };
    }

    /**
     * ✅ Handler d'échec avec logs détaillés
     */
    @Bean
    public AuthenticationFailureHandler adminFailureHandler() {
        return (request, response, exception) -> {
            String username = request.getParameter("username");
            log.warn("❌ [ADMIN] Échec connexion pour: '{}' - Raison: {}", username, exception.getMessage());
            log.warn("❌ [ADMIN] IP: {}", getClientIP(request));

            response.sendRedirect("/admin/login?error=true");
        };
    }

    /**
     * Utilitaire pour récupérer l'IP client
     */
    private String getClientIP(jakarta.servlet.http.HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}