/**
 * Controlador REST para gestionar cadenas.
 *
 * Autores:
 * - Alejandra Ortiz: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.grupo8.backend.dto.ChainRequestDto;
import es.grupo8.backend.dto.ChainResponseDto;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.ChainService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/chains")
@AllArgsConstructor
public class ChainController extends BaseRestController {

    private final ChainService chainService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<ChainResponseDto>> getChains(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        authService.checkAdmin(authHeader);
        List<ChainResponseDto> chains = chainService.findAll();
        return ResponseEntity.ok(chains);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChainResponseDto> getChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        authService.checkAdmin(authHeader);
        return ResponseEntity.ok(chainService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ChainResponseDto> createChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) ChainRequestDto req) {

        authService.checkAdmin(authHeader);
        ChainResponseDto created = chainService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChainResponseDto> updateChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) ChainRequestDto req) {

        authService.checkAdmin(authHeader);
        ChainResponseDto updated = chainService.update(id, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        authService.checkAdmin(authHeader);
        chainService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
