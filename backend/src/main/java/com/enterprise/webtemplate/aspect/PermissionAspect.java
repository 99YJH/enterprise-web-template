package com.enterprise.webtemplate.aspect;

import com.enterprise.webtemplate.annotation.RequirePermission;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String requiredPermission = requirePermission.value();
        
        boolean hasPermission = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(requiredPermission));

        if (!hasPermission) {
            throw new AccessDeniedException(requirePermission.message());
        }
    }

    @Before("@within(requirePermission)")
    public void checkClassPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        checkPermission(joinPoint, requirePermission);
    }
}