package com.ftabah.giftme.adapter.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 120;
    private final Map<String, ArrayDeque<Long>> requests = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }
        String key = request.getRemoteAddr();
        long now = Instant.now().toEpochMilli();
        ArrayDeque<Long> timestamps = requests.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        boolean allowed;
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= now - 60_000) {
                timestamps.removeFirst();
            }
            allowed = timestamps.size() < MAX_REQUESTS_PER_MINUTE;
            if (allowed) {
                timestamps.addLast(now);
            }
        }
        if (!allowed) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            return;
        }
        filterChain.doFilter(request, response);
    }
}