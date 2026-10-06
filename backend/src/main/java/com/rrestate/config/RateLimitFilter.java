package com.rrestate.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Per-IP sliding-window limiter for brute-force-sensitive endpoints. In-memory: correct for a single
 * instance; move to a shared store (e.g. Redis) before running multiple replicas.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {
    private static final long WINDOW_MS = 60_000;
    private static final Set<String> AUTH_PATHS = Set.of("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh");

    private final AppProperties.RateLimit limits;
    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public RateLimitFilter(AppProperties props) { this.limits = props.rateLimit(); }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        int limit = limitFor(req);
        if (limit > 0 && !allow(req.getRemoteAddr() + "|" + req.getRequestURI(), limit)) {
            res.setStatus(429);
            res.setHeader("Retry-After", "60");
            res.setContentType("application/json");
            res.getWriter().write("{\"status\":429,\"errorCode\":\"RATE_LIMITED\",\"message\":\"Too many requests. Please wait a minute and try again.\"}");
            return;
        }
        chain.doFilter(req, res);
    }

    private int limitFor(HttpServletRequest req) {
        if (!"POST".equals(req.getMethod())) return 0;
        String p = req.getRequestURI();
        if (AUTH_PATHS.contains(p)) return limits.authPerMinute();
        if ("/api/v1/investments".equals(p)) return limits.investmentsPerMinute();
        return 0;
    }

    private boolean allow(String key, int limit) {
        long now = System.currentTimeMillis();
        Deque<Long> q = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && now - q.peekFirst() > WINDOW_MS) q.pollFirst();
            if (q.size() >= limit) return false;
            q.addLast(now);
        }
        if (hits.size() > 10_000) hits.entrySet().removeIf(e -> { synchronized (e.getValue()) { return e.getValue().isEmpty() || now - e.getValue().peekLast() > WINDOW_MS; } });
        return true;
    }
}
