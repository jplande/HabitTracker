package com.habittracker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class LoggingMiddleware extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Génère un ID unique pour la requête
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        // Wrapper pour capturer le contenu
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Log de la requête entrante
            logRequest(requestWrapper, requestId);

            // Traitement de la requête
            filterChain.doFilter(requestWrapper, responseWrapper);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log de la réponse
            logResponse(responseWrapper, requestId, duration);

            // Important : copie le contenu vers la réponse originale
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(HttpServletRequest request, String requestId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String remoteAddr = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("📥 REQUÊTE [").append(requestId).append("] ");
        logMessage.append(method).append(" ").append(uri);

        if (queryString != null) {
            logMessage.append("?").append(queryString);
        }

        logMessage.append(" - IP: ").append(remoteAddr);

        // Log différent selon le type de requête
        if (isStaticResource(uri)) {
            log.debug(logMessage.toString());
        } else {
            log.info(logMessage.toString());
            if (userAgent != null && !userAgent.isEmpty()) {
                log.debug("📱 User-Agent [{}]: {}", requestId, userAgent);
            }
        }
    }

    private void logResponse(HttpServletResponse response, String requestId, long duration) {
        int status = response.getStatus();
        String statusEmoji = getStatusEmoji(status);

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("📤 RÉPONSE [").append(requestId).append("] ");
        logMessage.append(statusEmoji).append(" ").append(status);
        logMessage.append(" - ").append(duration).append("ms");

        // Log selon le niveau de statut
        if (status >= 500) {
            log.error(logMessage.toString());
        } else if (status >= 400) {
            log.warn(logMessage.toString());
        } else {
            log.info(logMessage.toString());
        }
    }

    private String getClientIP(HttpServletRequest request) {
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

    private String getStatusEmoji(int status) {
        if (status >= 200 && status < 300) return "✅";
        if (status >= 300 && status < 400) return "🔄";
        if (status >= 400 && status < 500) return "⚠️";
        if (status >= 500) return "❌";
        return "📄";
    }

    private boolean isStaticResource(String uri) {
        return uri.contains("/css/") || uri.contains("/js/") || uri.contains("/images/")
                || uri.contains("/favicon.ico") || uri.contains("/static/");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Ne pas logger les health checks et actuator pour éviter le spam
        return uri.equals("/actuator/health") || uri.startsWith("/actuator/metrics");
    }
}