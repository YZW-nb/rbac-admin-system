package com.admin.controller;

import com.admin.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控接口
 * 提供系统健康检查、性能指标等信息
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
@Tag(name = "系统监控", description = "系统健康检查和监控指标")
public class MonitorController {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查系统各组件健康状态")
    public Result<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        Map<String, Object> components = new HashMap<>();

        // 系统信息
        Map<String, Object> system = new HashMap<>();
        system.put("status", "UP");
        system.put("timestamp", LocalDateTime.now().format(FORMATTER));
        system.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        system.put("java", System.getProperty("java.version"));
        system.put("pid", ManagementFactory.getPid());
        components.put("system", system);

        // 数据库健康检查
        Map<String, Object> db = checkDatabase();
        components.put("database", db);

        // Redis 健康检查
        Map<String, Object> redis = checkRedis();
        components.put("redis", redis);

        // 内存信息
        Map<String, Object> memory = getMemoryInfo();
        components.put("memory", memory);

        health.put("status", isAllUp(db, redis) ? "UP" : "DEGRADED");
        health.put("components", components);

        return Result.success(health);
    }

    @GetMapping("/metrics")
    @Operation(summary = "性能指标", description = "获取系统性能指标")
    public Result<Map<String, Object>> metrics() {
        Map<String, Object> metrics = new HashMap<>();

        // 内存信息
        metrics.put("memory", getMemoryInfo());

        // 线程信息
        metrics.put("threads", getThreadInfo());

        // JVM 信息
        metrics.put("jvm", getJvmInfo());

        // 数据库连接信息
        metrics.put("datasource", getDataSourceInfo());

        return Result.success(metrics);
    }

    @GetMapping("/info")
    @Operation(summary = "系统信息", description = "获取系统基本信息")
    public Result<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("appName", "RBAC Admin");
        info.put("version", "1.0.0");
        info.put("description", "RBAC 权限管理系统");
        info.put("startTime", getStartTime());
        info.put("uptime", getUptime());
        info.put("environment", System.getProperty("spring.profiles.active", "unknown"));
        return Result.success(info);
    }

    private Map<String, Object> checkDatabase() {
        Map<String, Object> db = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            db.put("status", "UP");
            db.put("url", conn.getMetaData().getURL());
            db.put("database", conn.getCatalog());
            db.put("driver", conn.getMetaData().getDriverName());
            db.put("version", conn.getMetaData().getDriverVersion());
        } catch (Exception e) {
            log.error("数据库连接失败", e);
            db.put("status", "DOWN");
            db.put("error", e.getMessage());
        }
        return db;
    }

    private Map<String, Object> checkRedis() {
        Map<String, Object> redis = new HashMap<>();
        try {
            String pong = redisConnectionFactory.getConnection().ping();
            redis.put("status", "UP");
            redis.put("ping", pong);
        } catch (Exception e) {
            log.error("Redis 连接失败", e);
            redis.put("status", "DOWN");
            redis.put("error", e.getMessage());
        }
        return redis;
    }

    private Map<String, Object> getMemoryInfo() {
        Map<String, Object> memory = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();

        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;
        long max = runtime.maxMemory();

        memory.put("total", formatBytes(total));
        memory.put("used", formatBytes(used));
        memory.put("free", formatBytes(free));
        memory.put("max", formatBytes(max));
        memory.put("usagePercent", String.format("%.2f%%", (used * 100.0) / max));

        return memory;
    }

    private Map<String, Object> getThreadInfo() {
        Map<String, Object> threads = new HashMap<>();
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        while (threadGroup.getParent() != null) {
            threadGroup = threadGroup.getParent();
        }

        threads.put("count", threadGroup.activeCount());
        threads.put("active", threadGroup.activeCount());
        threads.put("peak", Runtime.getRuntime().availableProcessors() * 2);
        return threads;
    }

    private Map<String, Object> getJvmInfo() {
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("javaVersion", System.getProperty("java.version"));
        jvm.put("javaHome", System.getProperty("java.home"));
        jvm.put("pid", ManagementFactory.getPid());
        jvm.put("gcCount", getGcInfo());
        return jvm;
    }

    private Map<String, Object> getDataSourceInfo() {
        Map<String, Object> ds = new HashMap<>();
        try {
            var hikari = dataSource.getClass().getDeclaredField("hikariPool");
            hikari.setAccessible(true);
            Object pool = hikari.get(dataSource);
            if (pool != null) {
                ds.put("activeConnections", pool.getClass().getMethod("getActiveConnections").invoke(pool));
                ds.put("idleConnections", pool.getClass().getMethod("getIdleConnections").invoke(pool));
                ds.put("totalConnections", pool.getClass().getMethod("getTotalConnections").invoke(pool));
                ds.put("threadsAwaitingConnection", pool.getClass().getMethod("getThreadsAwaitingConnection").invoke(pool));
            }
        } catch (Exception e) {
            ds.put("status", "Unable to get details");
        }
        return ds;
    }

    private Map<String, Object> getGcInfo() {
        Map<String, Object> gc = new HashMap<>();
        long totalCount = 0;
        long totalTime = 0;
        for (var gcMxBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            totalCount += gcMxBean.getCollectionCount();
            totalTime += gcMxBean.getCollectionTime();
        }
        gc.put("totalCount", totalCount);
        gc.put("totalTime", totalTime + "ms");
        return gc;
    }

    private String getStartTime() {
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(startTime),
            java.time.ZoneId.systemDefault()
        ).format(FORMATTER);
    }

    private String getUptime() {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        long days = uptime / (1000 * 60 * 60 * 24);
        long hours = (uptime % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (uptime % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (uptime % (1000 * 60)) / 1000;
        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }

    private boolean isAllUp(Map<String, Object> db, Map<String, Object> redis) {
        return "UP".equals(db.get("status")) && "UP".equals(redis.get("status"));
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int unit = 0;
        double value = bytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        while (value >= 1024 && unit < units.length - 1) {
            value /= 1024;
            unit++;
        }
        return String.format("%.2f %s", value, units[unit]);
    }

    // 引入 ManagementFactory
    private static final java.lang.management.ManagementFactory ManagementFactory =
        java.lang.management.ManagementFactory.INSTANCE;
}
