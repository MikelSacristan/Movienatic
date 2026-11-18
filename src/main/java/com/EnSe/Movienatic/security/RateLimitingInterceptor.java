package com.EnSe.Movienatic.security;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.method.HandlerMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import com.EnSe.Movienatic.exception.RateLimitExceededException;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterAccess(Duration.ofHours(1))
            .build();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // Buscamos a anotación no método ou na clase do controlador
        RateLimited config = handlerMethod.getMethodAnnotation(RateLimited.class);
        if (config == null) {
            config = handlerMethod.getBeanType().getAnnotation(RateLimited.class);
        }
        if (config==null) {
            return true; // Endpoint sen límite específico
        }
        RateLimited config_tmp=config;
        // Clave combinando IP/Usuario + Nome do método destino
        String key = request.getRemoteAddr() + ":" + handlerMethod.getMethod().toGenericString();
        Bucket bucket = cache.get(key, k -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(config_tmp.capacity())
                        .refillGreedy(config_tmp.capacity(), Duration.of(config_tmp.duration(), config_tmp.unit()))
                        .build())
                .build());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        long waitSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
        response.addHeader("Retry-After", String.valueOf(waitSeconds));
        throw new RateLimitExceededException("Demasiadas peticións a este recurso", waitSeconds);
    }
}
