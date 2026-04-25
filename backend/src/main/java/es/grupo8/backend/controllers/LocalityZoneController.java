package es.grupo8.backend.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dao.GeographicZoneRepository;
import es.grupo8.backend.dao.LocalityRepository;
import es.grupo8.backend.security.AdminGuard;

@RestController
public class LocalityZoneController {

    @Autowired private LocalityRepository      localityRepository;
    @Autowired private GeographicZoneRepository zoneRepository;
    @Autowired private AdminGuard              adminGuard;

    @GetMapping("/api/localities")
    public ResponseEntity<?> getLocalities(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!adminGuard.isAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access restricted to administrators"));
        }

        List<Map<String, Object>> result = localityRepository.findAll()
                .stream()
                .map(l -> Map.<String, Object>of(
                        "id",     l.getId(),
                        "name",   l.getName(),
                        "zoneId", l.getIdZone() != null ? l.getIdZone().getId() : null
                ))
                .sorted((a, b) -> String.valueOf(a.get("name")).compareTo(String.valueOf(b.get("name"))))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/zones")
    public ResponseEntity<?> getZones(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!adminGuard.isAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access restricted to administrators"));
        }

        List<Map<String, Object>> result = zoneRepository.findAll()
                .stream()
                .map(z -> Map.<String, Object>of(
                        "id",   z.getId(),
                        "name", z.getName()
                ))
                .sorted((a, b) -> String.valueOf(a.get("name")).compareTo(String.valueOf(b.get("name"))))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}