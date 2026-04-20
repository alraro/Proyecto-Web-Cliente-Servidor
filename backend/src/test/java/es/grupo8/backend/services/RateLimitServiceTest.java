package es.grupo8.backend.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitServiceTest {

    @Test
    void blocksAfterConfiguredAttempts() {
        RateLimitService service = new RateLimitService(3, 60_000, 60_000);
        String key = "user@127.0.0.1";

        assertFalse(service.isBlocked(key));

        service.registerFailure(key);
        service.registerFailure(key);
        assertFalse(service.isBlocked(key));

        service.registerFailure(key);
        assertTrue(service.isBlocked(key));
    }

    @Test
    void successResetsRateLimitState() {
        RateLimitService service = new RateLimitService(2, 60_000, 60_000);
        String key = "user@127.0.0.1";

        service.registerFailure(key);
        service.registerFailure(key);
        assertTrue(service.isBlocked(key));

        service.registerSuccess(key);
        assertFalse(service.isBlocked(key));
    }
}
