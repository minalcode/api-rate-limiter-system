
package com.ratelimiter.api_rate_limiter_system.Controller;

import com.ratelimiter.api_rate_limiter_system.Service.RateLimiterService;
import com.ratelimiter.api_rate_limiter_system.Service.RateLimiterService.RateLimitResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RateLimiterController {

    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> demo(
            @RequestHeader(value = "X-API-KEY",
                    defaultValue = "anonymous") String apiKey) {

        RateLimitResult result =
                rateLimiterService.checkRequest(apiKey, "/api/demo");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("limit", result.limit());
        body.put("remaining", result.remaining());

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RateLimit-Limit",
                String.valueOf(result.limit()));
        headers.set("X-RateLimit-Remaining",
                String.valueOf(result.remaining()));

        if (!result.allowed()) {
            headers.set("Retry-After",
                    String.valueOf(result.retryAfterSeconds()));

            body.put("message", "Rate limit exceeded. Try again later.");

            return new ResponseEntity<>(
                    body, headers, HttpStatus.TOO_MANY_REQUESTS);
        }

        body.put("message", "Request allowed successfully");

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}
