package com.habittracker.config;

import com.habittracker.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité pour les controllers MVC d'administration
 * SIMPLIFIÉ - pas de conflit avec SecurityConfig
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class AdminWebSecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Configuration de sécurité pour les routes d'administration MVC
     * Order(1) pour s'assurer que cette config s'applique avant la config API
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                // Appliquer cette config uniquement aux routes /admin/**
                .securityMatcher("/admin/**")

                // Configuration des autorisations
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/admin/login", "/admin/access-denied").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // Configuration de l'authentification par formulaire pour MVC
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin/dashboard", true)
                        .failureUrl("/admin/login?error=true")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )

                // Configuration de la déconnexion
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // Gestion des erreurs d'accès
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/admin/access-denied")
                )

                // Configuration des sessions pour l'admin
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(2) // Permettre 2 sessions admin simultanées
                        .maxSessionsPreventsLogin(false)
                )

                // Activer CSRF pour les formulaires MVC
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")); // Désactiver CSRF pour l'API seulement

        return http.build();
    }

    /**
     * Configuration pour les ressources statiques (CSS, JS, images)
     */
    @Bean
    @Order(0)
    public SecurityFilterChain staticResourcesFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/css/**", "/js/**", "/images/**", "/favicon.ico", "/webjars/**")
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * Provider d'authentification pour les formulaires MVC
     * Nom différent pour éviter les conflits
     */
    @Bean
    public DaoAuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

}
