package com.enterprise.webtemplate.validation;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

@Component
public class InputSanitizer {

    // XSS 방지를 위한 위험한 패턴들
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    // SQL Injection 방지를 위한 위험한 패턴들
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        Pattern.compile("('|(\\-\\-)|(;)|(\\|)|(\\*)|(%))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(union|select|insert|delete|update|drop|create|alter|exec|execute)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(sp_|xp_)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\b(or|and)\\b.*(=|>|<|!=|<>|>=|<=))", Pattern.CASE_INSENSITIVE)
    };

    // Path Traversal 방지를 위한 패턴
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("(\\.\\./|\\.\\.\\\\|%2e%2e%2f|%2e%2e%5c)", Pattern.CASE_INSENSITIVE);

    /**
     * XSS 공격 방지를 위한 HTML 이스케이프
     */
    public String sanitizeForXss(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        String sanitized = HtmlUtils.htmlEscape(input);
        
        // 추가적인 XSS 패턴 제거
        for (Pattern pattern : XSS_PATTERNS) {
            sanitized = pattern.matcher(sanitized).replaceAll("");
        }
        
        return sanitized;
    }

    /**
     * SQL Injection 공격 방지를 위한 입력 검증
     */
    public boolean containsSqlInjection(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        String normalizedInput = input.toLowerCase().trim();
        
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(normalizedInput).find()) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Path Traversal 공격 방지를 위한 경로 검증
     */
    public boolean containsPathTraversal(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        return PATH_TRAVERSAL_PATTERN.matcher(input).find();
    }

    /**
     * 파일명 안전성 검증
     */
    public String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }
        
        // 위험한 문자 제거
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9._-]", "");
        
        // 확장자 검증
        if (sanitized.contains("..")) {
            sanitized = sanitized.replace("..", ".");
        }
        
        // 길이 제한
        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }
        
        return sanitized;
    }

    /**
     * 이메일 주소 검증
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        Pattern emailPattern = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        );
        
        return emailPattern.matcher(email).matches();
    }

    /**
     * 전화번호 검증 (한국 형식)
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        
        // 한국 전화번호 형식 검증
        Pattern phonePattern = Pattern.compile(
            "^(\\+82|0)?(2|3[1-3]|4[1-4]|5[1-5]|6[1-4]|70|8[0-9]|9[0-9])[-]?[0-9]{3,4}[-]?[0-9]{4}$"
        );
        
        return phonePattern.matcher(phoneNumber.replaceAll("\\s+", "")).matches();
    }

    /**
     * 비밀번호 강도 검증
     */
    public boolean isStrongPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        // 최소 8자, 최대 128자
        if (password.length() < 8 || password.length() > 128) {
            return false;
        }
        
        // 대문자, 소문자, 숫자, 특수문자 포함
        Pattern uppercasePattern = Pattern.compile("[A-Z]");
        Pattern lowercasePattern = Pattern.compile("[a-z]");
        Pattern digitPattern = Pattern.compile("[0-9]");
        Pattern specialCharPattern = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
        
        return uppercasePattern.matcher(password).find() &&
               lowercasePattern.matcher(password).find() &&
               digitPattern.matcher(password).find() &&
               specialCharPattern.matcher(password).find();
    }

    /**
     * 일반 텍스트 입력 검증 및 정제
     */
    public String sanitizeText(String input) {
        if (input == null) {
            return null;
        }
        
        // XSS 방지
        String sanitized = sanitizeForXss(input);
        
        // 앞뒤 공백 제거
        sanitized = sanitized.trim();
        
        // 연속된 공백을 하나로 변경
        sanitized = sanitized.replaceAll("\\s+", " ");
        
        return sanitized;
    }

    /**
     * 숫자 입력 검증
     */
    public boolean isValidNumber(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * URL 검증
     */
    public boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        Pattern urlPattern = Pattern.compile(
            "^(https?|ftp)://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(:[0-9]+)?(/.*)?$"
        );
        
        return urlPattern.matcher(url).matches();
    }
}