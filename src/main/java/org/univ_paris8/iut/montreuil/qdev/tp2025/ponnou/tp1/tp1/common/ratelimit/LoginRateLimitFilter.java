package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ErrorResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/auth/login";

    private final int capacity;
    private final long refillIntervalMs;
    private final ObjectMapper objectMapper;

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public LoginRateLimitFilter(
            @Value("${rate-limit.login.capacity:5}") int capacity,
            @Value("${rate-limit.login.refill-seconds:60}") int refillSeconds,
            ObjectMapper objectMapper) {
        this.capacity = capacity;
        this.refillIntervalMs = (long) refillSeconds * 1000;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && requestPath.startsWith(contextPath)) {
            requestPath = requestPath.substring(contextPath.length());
        }
        return !LOGIN_PATH.equals(requestPath);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = resolveClientIp(request);
        TokenBucket bucket = buckets.computeIfAbsent(clientIp, ip -> new TokenBucket(capacity, refillIntervalMs));

        if (bucket.tryConsume()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                    new ErrorResponse("TOO_MANY_REQUESTS", "Trop de tentatives de connexion. Reessayez plus tard."));
        }
    }

    public void clearBuckets() {
        buckets.clear();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    static class TokenBucket {
        private final int capacity;
        private final long refillIntervalMs;
        private int tokens;
        private long lastRefillTime;

        TokenBucket(int capacity, long refillIntervalMs) {
            this.capacity = capacity;
            this.refillIntervalMs = refillIntervalMs;
            this.tokens = capacity;
            this.lastRefillTime = Instant.now().toEpochMilli();
        }

        synchronized boolean tryConsume() {
            refillIfNeeded();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refillIfNeeded() {
            long now = Instant.now().toEpochMilli();
            if (now - lastRefillTime >= refillIntervalMs) {
                tokens = capacity;
                lastRefillTime = now;
            }
        }
    }
}
