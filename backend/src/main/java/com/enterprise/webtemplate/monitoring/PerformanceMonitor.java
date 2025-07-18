package com.enterprise.webtemplate.monitoring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
public class PerformanceMonitor {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    private static final Logger performanceLogger = LoggerFactory.getLogger("performance");

    @Autowired
    private MetricsCollector metricsCollector;

    private final ConcurrentHashMap<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> totalExecutionTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();

    @Around("@annotation(Monitored)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();
        
        // 메모리 사용량 기록 (시작 시점)
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage beforeMemory = memoryBean.getHeapMemoryUsage();
        
        try {
            Object result = joinPoint.proceed();
            
            // 성공 시 메트릭 수집
            long executionTime = System.currentTimeMillis() - startTime;
            recordSuccessMetrics(methodName, executionTime);
            
            return result;
            
        } catch (Exception e) {
            // 실패 시 메트릭 수집
            long executionTime = System.currentTimeMillis() - startTime;
            recordErrorMetrics(methodName, executionTime, e);
            
            throw e;
        } finally {
            // 메모리 사용량 기록 (종료 시점)
            MemoryUsage afterMemory = memoryBean.getHeapMemoryUsage();
            long memoryUsed = afterMemory.getUsed() - beforeMemory.getUsed();
            
            if (memoryUsed > 0) {
                metricsCollector.recordMemoryUsage(methodName, memoryUsed);
            }
        }
    }

    private void recordSuccessMetrics(String methodName, long executionTime) {
        requestCounts.computeIfAbsent(methodName, k -> new AtomicLong(0)).incrementAndGet();
        totalExecutionTime.computeIfAbsent(methodName, k -> new AtomicLong(0)).addAndGet(executionTime);
        
        // 성능 로그 기록
        performanceLogger.info("Method: {}, ExecutionTime: {}ms", methodName, executionTime);
        
        // 느린 쿼리 감지 (5초 이상)
        if (executionTime > 5000) {
            logger.warn("Slow method detected: {} took {}ms", methodName, executionTime);
        }
        
        // 메트릭 수집기에 데이터 전송
        metricsCollector.recordMethodExecution(methodName, executionTime, true);
    }

    private void recordErrorMetrics(String methodName, long executionTime, Exception error) {
        errorCounts.computeIfAbsent(methodName, k -> new AtomicLong(0)).incrementAndGet();
        
        // 에러 로그 기록
        logger.error("Method: {} failed after {}ms with error: {}", 
                    methodName, executionTime, error.getMessage());
        
        // 메트릭 수집기에 에러 데이터 전송
        metricsCollector.recordMethodExecution(methodName, executionTime, false);
        metricsCollector.recordError(methodName, error.getClass().getSimpleName(), error.getMessage());
    }

    /**
     * 메서드별 성능 통계 조회
     */
    public PerformanceStats getPerformanceStats(String methodName) {
        AtomicLong requests = requestCounts.get(methodName);
        AtomicLong totalTime = totalExecutionTime.get(methodName);
        AtomicLong errors = errorCounts.get(methodName);
        
        if (requests == null || requests.get() == 0) {
            return new PerformanceStats(methodName, 0, 0, 0, 0);
        }
        
        long requestCount = requests.get();
        long averageTime = totalTime.get() / requestCount;
        long errorCount = errors != null ? errors.get() : 0;
        double errorRate = (double) errorCount / requestCount * 100;
        
        return new PerformanceStats(methodName, requestCount, averageTime, errorCount, errorRate);
    }

    /**
     * 모든 메서드의 성능 통계 조회
     */
    public java.util.Map<String, PerformanceStats> getAllPerformanceStats() {
        java.util.Map<String, PerformanceStats> stats = new java.util.HashMap<>();
        
        for (String methodName : requestCounts.keySet()) {
            stats.put(methodName, getPerformanceStats(methodName));
        }
        
        return stats;
    }

    /**
     * 성능 통계 초기화
     */
    public void resetStats() {
        requestCounts.clear();
        totalExecutionTime.clear();
        errorCounts.clear();
        logger.info("Performance statistics have been reset");
    }

    /**
     * 시스템 리소스 사용량 모니터링
     */
    public SystemResourceUsage getSystemResourceUsage() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
        
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return new SystemResourceUsage(
            heapMemory.getUsed(),
            heapMemory.getMax(),
            nonHeapMemory.getUsed(),
            nonHeapMemory.getMax(),
            usedMemory,
            maxMemory,
            ManagementFactory.getOperatingSystemMXBean().getProcessCpuLoad()
        );
    }

    /**
     * 성능 통계 데이터 클래스
     */
    public static class PerformanceStats {
        private final String methodName;
        private final long requestCount;
        private final long averageExecutionTime;
        private final long errorCount;
        private final double errorRate;

        public PerformanceStats(String methodName, long requestCount, long averageExecutionTime, 
                               long errorCount, double errorRate) {
            this.methodName = methodName;
            this.requestCount = requestCount;
            this.averageExecutionTime = averageExecutionTime;
            this.errorCount = errorCount;
            this.errorRate = errorRate;
        }

        // Getters
        public String getMethodName() { return methodName; }
        public long getRequestCount() { return requestCount; }
        public long getAverageExecutionTime() { return averageExecutionTime; }
        public long getErrorCount() { return errorCount; }
        public double getErrorRate() { return errorRate; }
    }

    /**
     * 시스템 리소스 사용량 데이터 클래스
     */
    public static class SystemResourceUsage {
        private final long heapMemoryUsed;
        private final long heapMemoryMax;
        private final long nonHeapMemoryUsed;
        private final long nonHeapMemoryMax;
        private final long totalMemoryUsed;
        private final long totalMemoryMax;
        private final double cpuUsage;

        public SystemResourceUsage(long heapMemoryUsed, long heapMemoryMax, 
                                  long nonHeapMemoryUsed, long nonHeapMemoryMax,
                                  long totalMemoryUsed, long totalMemoryMax, 
                                  double cpuUsage) {
            this.heapMemoryUsed = heapMemoryUsed;
            this.heapMemoryMax = heapMemoryMax;
            this.nonHeapMemoryUsed = nonHeapMemoryUsed;
            this.nonHeapMemoryMax = nonHeapMemoryMax;
            this.totalMemoryUsed = totalMemoryUsed;
            this.totalMemoryMax = totalMemoryMax;
            this.cpuUsage = cpuUsage;
        }

        // Getters
        public long getHeapMemoryUsed() { return heapMemoryUsed; }
        public long getHeapMemoryMax() { return heapMemoryMax; }
        public long getNonHeapMemoryUsed() { return nonHeapMemoryUsed; }
        public long getNonHeapMemoryMax() { return nonHeapMemoryMax; }
        public long getTotalMemoryUsed() { return totalMemoryUsed; }
        public long getTotalMemoryMax() { return totalMemoryMax; }
        public double getCpuUsage() { return cpuUsage; }
    }
}