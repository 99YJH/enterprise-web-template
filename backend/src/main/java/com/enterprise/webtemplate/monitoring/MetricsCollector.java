package com.enterprise.webtemplate.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MetricsCollector {

    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);
    private static final Logger metricsLogger = LoggerFactory.getLogger("metrics");
    
    // 메트릭 데이터 저장소
    private final ConcurrentHashMap<String, AtomicLong> methodExecutionCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> methodExecutionTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> methodErrorCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> memoryUsageByMethod = new ConcurrentHashMap<>();
    
    // 에러 로그 저장소
    private final ConcurrentLinkedQueue<ErrorRecord> errorRecords = new ConcurrentLinkedQueue<>();
    
    // 시스템 메트릭
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalMemoryAllocated = new AtomicLong(0);

    /**
     * 메서드 실행 정보 기록
     */
    @Async
    public void recordMethodExecution(String methodName, long executionTime, boolean success) {
        methodExecutionCounts.computeIfAbsent(methodName, k -> new AtomicLong(0)).incrementAndGet();
        methodExecutionTimes.computeIfAbsent(methodName, k -> new AtomicLong(0)).addAndGet(executionTime);
        
        totalRequests.incrementAndGet();
        
        if (!success) {
            methodErrorCounts.computeIfAbsent(methodName, k -> new AtomicLong(0)).incrementAndGet();
            totalErrors.incrementAndGet();
        }
        
        // 메트릭 로그 기록
        metricsLogger.info("method={}, executionTime={}, success={}, timestamp={}", 
                          methodName, executionTime, success, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * 메모리 사용량 기록
     */
    @Async
    public void recordMemoryUsage(String methodName, long memoryUsed) {
        memoryUsageByMethod.computeIfAbsent(methodName, k -> new AtomicLong(0)).addAndGet(memoryUsed);
        totalMemoryAllocated.addAndGet(memoryUsed);
        
        // 메모리 사용량이 많은 경우 경고
        if (memoryUsed > 50 * 1024 * 1024) { // 50MB 이상
            logger.warn("High memory usage detected: {} used {} bytes", methodName, memoryUsed);
        }
    }

    /**
     * 에러 정보 기록
     */
    @Async
    public void recordError(String methodName, String errorType, String errorMessage) {
        ErrorRecord errorRecord = new ErrorRecord(
            methodName, 
            errorType, 
            errorMessage, 
            LocalDateTime.now()
        );
        
        errorRecords.offer(errorRecord);
        
        // 에러 큐 크기 제한 (최대 1000개)
        while (errorRecords.size() > 1000) {
            errorRecords.poll();
        }
        
        // 에러 로그 기록
        metricsLogger.error("method={}, errorType={}, errorMessage={}, timestamp={}", 
                           methodName, errorType, errorMessage, errorRecord.getTimestamp());
    }

    /**
     * 메트릭 요약 정보 조회
     */
    public MetricsSummary getMetricsSummary() {
        MetricsSummary summary = new MetricsSummary();
        
        summary.setTotalRequests(totalRequests.get());
        summary.setTotalErrors(totalErrors.get());
        summary.setTotalMemoryAllocated(totalMemoryAllocated.get());
        summary.setErrorRate(totalRequests.get() > 0 ? 
                            (double) totalErrors.get() / totalRequests.get() * 100 : 0);
        
        // 메서드별 통계
        for (String methodName : methodExecutionCounts.keySet()) {
            long count = methodExecutionCounts.get(methodName).get();
            long totalTime = methodExecutionTimes.get(methodName).get();
            long errors = methodErrorCounts.getOrDefault(methodName, new AtomicLong(0)).get();
            long memoryUsed = memoryUsageByMethod.getOrDefault(methodName, new AtomicLong(0)).get();
            
            MethodMetrics methodMetrics = new MethodMetrics(
                methodName,
                count,
                totalTime,
                count > 0 ? totalTime / count : 0,
                errors,
                count > 0 ? (double) errors / count * 100 : 0,
                memoryUsed
            );
            
            summary.addMethodMetrics(methodMetrics);
        }
        
        return summary;
    }

    /**
     * 최근 에러 목록 조회
     */
    public java.util.List<ErrorRecord> getRecentErrors(int limit) {
        return errorRecords.stream()
                          .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                          .limit(limit)
                          .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 메트릭 데이터 초기화
     */
    public void clearMetrics() {
        methodExecutionCounts.clear();
        methodExecutionTimes.clear();
        methodErrorCounts.clear();
        memoryUsageByMethod.clear();
        errorRecords.clear();
        
        totalRequests.set(0);
        totalErrors.set(0);
        totalMemoryAllocated.set(0);
        
        logger.info("All metrics have been cleared");
    }

    /**
     * 주기적으로 메트릭 요약 정보 로그 출력
     */
    @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void logMetricsSummary() {
        MetricsSummary summary = getMetricsSummary();
        
        logger.info("=== Metrics Summary ===");
        logger.info("Total Requests: {}", summary.getTotalRequests());
        logger.info("Total Errors: {}", summary.getTotalErrors());
        logger.info("Error Rate: {:.2f}%", summary.getErrorRate());
        logger.info("Total Memory Allocated: {} bytes", summary.getTotalMemoryAllocated());
        logger.info("========================");
    }

    /**
     * 에러 기록 클래스
     */
    public static class ErrorRecord {
        private final String methodName;
        private final String errorType;
        private final String errorMessage;
        private final LocalDateTime timestamp;

        public ErrorRecord(String methodName, String errorType, String errorMessage, LocalDateTime timestamp) {
            this.methodName = methodName;
            this.errorType = errorType;
            this.errorMessage = errorMessage;
            this.timestamp = timestamp;
        }

        // Getters
        public String getMethodName() { return methodName; }
        public String getErrorType() { return errorType; }
        public String getErrorMessage() { return errorMessage; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * 메서드별 메트릭 클래스
     */
    public static class MethodMetrics {
        private final String methodName;
        private final long requestCount;
        private final long totalExecutionTime;
        private final long averageExecutionTime;
        private final long errorCount;
        private final double errorRate;
        private final long memoryUsed;

        public MethodMetrics(String methodName, long requestCount, long totalExecutionTime, 
                            long averageExecutionTime, long errorCount, double errorRate, 
                            long memoryUsed) {
            this.methodName = methodName;
            this.requestCount = requestCount;
            this.totalExecutionTime = totalExecutionTime;
            this.averageExecutionTime = averageExecutionTime;
            this.errorCount = errorCount;
            this.errorRate = errorRate;
            this.memoryUsed = memoryUsed;
        }

        // Getters
        public String getMethodName() { return methodName; }
        public long getRequestCount() { return requestCount; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public long getAverageExecutionTime() { return averageExecutionTime; }
        public long getErrorCount() { return errorCount; }
        public double getErrorRate() { return errorRate; }
        public long getMemoryUsed() { return memoryUsed; }
    }

    /**
     * 메트릭 요약 클래스
     */
    public static class MetricsSummary {
        private long totalRequests;
        private long totalErrors;
        private long totalMemoryAllocated;
        private double errorRate;
        private final java.util.List<MethodMetrics> methodMetrics = new java.util.ArrayList<>();

        // Getters and Setters
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }

        public long getTotalErrors() { return totalErrors; }
        public void setTotalErrors(long totalErrors) { this.totalErrors = totalErrors; }

        public long getTotalMemoryAllocated() { return totalMemoryAllocated; }
        public void setTotalMemoryAllocated(long totalMemoryAllocated) { this.totalMemoryAllocated = totalMemoryAllocated; }

        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }

        public java.util.List<MethodMetrics> getMethodMetrics() { return methodMetrics; }
        public void addMethodMetrics(MethodMetrics metrics) { this.methodMetrics.add(metrics); }
    }
}