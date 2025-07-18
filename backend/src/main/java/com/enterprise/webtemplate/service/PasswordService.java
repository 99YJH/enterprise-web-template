package com.enterprise.webtemplate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class PasswordService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[@$!%*?&].*");

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public PasswordValidationResult validatePassword(String password) {
        PasswordValidationResult result = new PasswordValidationResult();
        
        if (password == null || password.isEmpty()) {
            result.addError("비밀번호를 입력해주세요.");
            return result;
        }

        if (password.length() < MIN_LENGTH) {
            result.addError("비밀번호는 최소 " + MIN_LENGTH + "자 이상이어야 합니다.");
        }

        if (password.length() > MAX_LENGTH) {
            result.addError("비밀번호는 최대 " + MAX_LENGTH + "자를 초과할 수 없습니다.");
        }

        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            result.addError("비밀번호는 소문자를 포함해야 합니다.");
        }

        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            result.addError("비밀번호는 대문자를 포함해야 합니다.");
        }

        if (!DIGIT_PATTERN.matcher(password).matches()) {
            result.addError("비밀번호는 숫자를 포함해야 합니다.");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            result.addError("비밀번호는 특수문자(@$!%*?&)를 포함해야 합니다.");
        }

        if (hasCommonPattern(password)) {
            result.addError("너무 간단한 패턴의 비밀번호는 사용할 수 없습니다.");
        }

        return result;
    }

    private boolean hasCommonPattern(String password) {
        String lower = password.toLowerCase();
        
        // 연속된 문자나 숫자 패턴 검사
        for (int i = 0; i < lower.length() - 2; i++) {
            char first = lower.charAt(i);
            char second = lower.charAt(i + 1);
            char third = lower.charAt(i + 2);
            
            // 연속된 문자 (abc, 123)
            if (second == first + 1 && third == second + 1) {
                return true;
            }
            
            // 동일한 문자 (aaa, 111)
            if (first == second && second == third) {
                return true;
            }
        }
        
        // 일반적인 패턴들
        String[] commonPatterns = {
            "password", "123456", "qwerty", "admin", "letmein",
            "welcome", "monkey", "dragon", "master", "secret"
        };
        
        for (String pattern : commonPatterns) {
            if (lower.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    public static class PasswordValidationResult {
        private boolean valid = true;
        private java.util.List<String> errors = new java.util.ArrayList<>();

        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }

        public boolean isValid() {
            return valid;
        }

        public java.util.List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return String.join(", ", errors);
        }
    }
}