package es.grupo8.backend.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfServiceTest {

    @Test
    void tokenIsSingleUse() {
        CsrfService service = new CsrfService(60_000);

        String token = service.generateToken();
        assertNotNull(token);

        assertTrue(service.validateAndConsume(token));
        assertFalse(service.validateAndConsume(token));
    }
}
