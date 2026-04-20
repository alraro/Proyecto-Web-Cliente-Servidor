package es.grupo8.backend.services;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitService {

    private static class State {
        int attempts;
        long windowStart;
        long blockedUntil;
    }

    private final int maxAttempts;
    private final long windowMs;
    private final long blockMs;
    private final Map<String, State> states = new ConcurrentHashMap<>();

    public RateLimitService(int maxAttempts, long windowMs, long blockMs) {
        this.maxAttempts = maxAttempts;
        this.windowMs = windowMs;
        this.blockMs = blockMs;
    }

    public void registerFailure(String key) {
        long now = Instant.now().toEpochMilli();
        State state = states.computeIfAbsent(key, k -> {
            State created = new State();
            created.windowStart = now;
            return created;
        });

        if (now - state.windowStart > windowMs) {
            state.attempts = 0;
            state.windowStart = now;
            state.blockedUntil = 0;
        }

        state.attempts++;

        if (state.attempts >= maxAttempts) {
            state.blockedUntil = now + blockMs;
        }
    }

    public void registerSuccess(String key) {
        states.remove(key);
    }

    public boolean isBlocked(String key) {
        State state = states.get(key);
        if (state == null) {
            return false;
        }

        long now = Instant.now().toEpochMilli();
        if (now > state.blockedUntil) {
            if (now - state.windowStart > windowMs) {
                states.remove(key);
            }
            return false;
        }

        return true;
    }
}
