package com.enterprise.webtemplate.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

@Component
public class SafeInputValidator implements ConstraintValidator<SafeInput, String> {

    @Autowired
    private InputSanitizer inputSanitizer;

    private boolean checkXss;
    private boolean checkSqlInjection;
    private boolean checkPathTraversal;
    private int maxLength;
    private int minLength;
    private String pattern;

    @Override
    public void initialize(SafeInput constraintAnnotation) {
        this.checkXss = constraintAnnotation.checkXss();
        this.checkSqlInjection = constraintAnnotation.checkSqlInjection();
        this.checkPathTraversal = constraintAnnotation.checkPathTraversal();
        this.maxLength = constraintAnnotation.maxLength();
        this.minLength = constraintAnnotation.minLength();
        this.pattern = constraintAnnotation.pattern();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return minLength == 0; // 빈 값이 허용되는지 확인
        }

        // 길이 검증
        if (value.length() < minLength || value.length() > maxLength) {
            addConstraintViolation(context, "입력값의 길이가 유효 범위(" + minLength + "-" + maxLength + ")를 벗어났습니다.");
            return false;
        }

        // XSS 검증
        if (checkXss && !value.equals(inputSanitizer.sanitizeForXss(value))) {
            addConstraintViolation(context, "XSS 공격이 의심되는 입력값입니다.");
            return false;
        }

        // SQL Injection 검증
        if (checkSqlInjection && inputSanitizer.containsSqlInjection(value)) {
            addConstraintViolation(context, "SQL Injection 공격이 의심되는 입력값입니다.");
            return false;
        }

        // Path Traversal 검증
        if (checkPathTraversal && inputSanitizer.containsPathTraversal(value)) {
            addConstraintViolation(context, "Path Traversal 공격이 의심되는 입력값입니다.");
            return false;
        }

        // 정규식 패턴 검증
        if (!pattern.isEmpty()) {
            try {
                Pattern compiledPattern = Pattern.compile(pattern);
                if (!compiledPattern.matcher(value).matches()) {
                    addConstraintViolation(context, "입력값이 허용된 형식과 일치하지 않습니다.");
                    return false;
                }
            } catch (Exception e) {
                addConstraintViolation(context, "정규식 패턴 검증 중 오류가 발생했습니다.");
                return false;
            }
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}