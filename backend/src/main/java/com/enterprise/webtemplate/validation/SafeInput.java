package com.enterprise.webtemplate.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SafeInputValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeInput {
    
    String message() default "입력값이 안전하지 않습니다.";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    // XSS 검증 여부
    boolean checkXss() default true;
    
    // SQL Injection 검증 여부
    boolean checkSqlInjection() default true;
    
    // Path Traversal 검증 여부
    boolean checkPathTraversal() default false;
    
    // 최대 길이
    int maxLength() default 1000;
    
    // 최소 길이
    int minLength() default 0;
    
    // 정규식 패턴
    String pattern() default "";
}