package com.enterprise.webtemplate.monitoring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 성능 모니터링을 위한 어노테이션
 * 이 어노테이션이 붙은 메서드는 실행 시간, 메모리 사용량, 에러율 등이 모니터링됩니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitored {
    
    /**
     * 모니터링 설명
     */
    String description() default "";
    
    /**
     * 느린 실행 시간 임계값 (밀리초)
     * 이 값을 초과하면 경고 로그가 생성됩니다.
     */
    long slowThreshold() default 5000;
    
    /**
     * 메모리 사용량 모니터링 여부
     */
    boolean monitorMemory() default true;
    
    /**
     * 에러율 모니터링 여부
     */
    boolean monitorErrors() default true;
}