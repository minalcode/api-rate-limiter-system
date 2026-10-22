
package com.ratelimiter.api_rate_limiter_system.Service;

import com.ratelimiter.api_rate_limiter_system.Model.RequestLog;
import com.ratelimiter.api_rate_limiter_system.Repository.RequestLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final int REQUEST_LIMIT = 5;
    private static final long WINDOW_MILLIS = 60_000;

    private final RequestLogRepository requestLogRepository;

    private final ConcurrentHashMap<String, Window> clients =
            new ConcurrentHashMap<>();

    public RateLimiterService(RequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    public synchronized RateLimitResult checkRequest(
            String clientKey, String endpoint) {

        long now = System.currentTimeMillis();

        Window window = clients.get(clientKey);

        if (window == null || now - window.startTime >= WINDOW_MILLIS) {
            window = new Window(now);
            clients.put(clientKey, window);
        }

        boolean allowed = window.requestCount < REQUEST_LIMIT;

        if (allowed) {
            window.requestCount++;
        }

        int remaining = Math.max(
                0, REQUEST_LIMIT - window.requestCount
        );

        long retryAfter = Math.max(
                1,
                (window.startTime + WINDOW_MILLIS - now + 999) / 1000
        );

        RequestLog log = new RequestLog(
                null,
                clientKey,
                endpoint,
                LocalDateTime.now(),
                allowed
        );

        requestLogRepository.save(log);

        return new RateLimitResult(
                allowed,
                REQUEST_LIMIT,
                remaining,
                retryAfter
        );
    }

    private static class Window {
        private final long startTime;
        private int requestCount;

        private Window(long startTime) {
            this.startTime = startTime;
            this.requestCount = 0;
        }
    }

    public record RateLimitResult(
            boolean allowed,
            int limit,
            int remaining,
            long retryAfterSeconds
    ) {
    }
}
