package com.enterprise.webtemplate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SecurityHeadersConfig {

    @Bean
    public SecurityHeadersFilter securityHeadersFilter() {
        return new SecurityHeadersFilter();
    }

    public static class SecurityHeadersFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        FilterChain filterChain) throws ServletException, IOException {
            
            // Content Security Policy
            response.setHeader("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' data:; " +
                "connect-src 'self' ws: wss:; " +
                "frame-ancestors 'none'; " +
                "base-uri 'self'; " +
                "form-action 'self'");
            
            // X-Frame-Options
            response.setHeader("X-Frame-Options", "DENY");
            
            // X-Content-Type-Options
            response.setHeader("X-Content-Type-Options", "nosniff");
            
            // X-XSS-Protection
            response.setHeader("X-XSS-Protection", "1; mode=block");
            
            // Referrer Policy
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            
            // Strict-Transport-Security (HSTS)
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            
            // Permissions Policy
            response.setHeader("Permissions-Policy", 
                "camera=(), " +
                "microphone=(), " +
                "geolocation=(), " +
                "interest-cohort=()");
            
            // Cross-Origin-Embedder-Policy
            response.setHeader("Cross-Origin-Embedder-Policy", "require-corp");
            
            // Cross-Origin-Opener-Policy
            response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
            
            // Cross-Origin-Resource-Policy
            response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
            
            // Cache-Control for sensitive endpoints
            String requestURI = request.getRequestURI();
            if (requestURI.contains("/api/auth/") || requestURI.contains("/api/admin/")) {
                response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
            }
            
            // Server header 제거
            response.setHeader("Server", "");
            
            filterChain.doFilter(request, response);
        }
    }
}