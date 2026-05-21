package es.grupo8.backend.controllers;

import es.grupo8.backend.dto.VoluntarioRequestDto;
import es.grupo8.backend.dto.VoluntarioResponseDto;
import es.grupo8.backend.services.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/voluntarios")
public class VolunteerController {

    @Autowired
    private VolunteerService volunteerService;

    @GetMapping
    public ResponseEntity<List<VoluntarioResponseDto>> listarVoluntarios(
            @RequestParam Long entidadId) {

        List<VoluntarioResponseDto> voluntarios = volunteerService.getVolunteersByEntity(entidadId.intValue());
        return ResponseEntity.ok(voluntarios);
    }

    @PostMapping
    public ResponseEntity<VoluntarioResponseDto> crearVoluntario(
            @RequestBody VoluntarioRequestDto request,
            @RequestParam Long entidadId) {

        VoluntarioResponseDto voluntario = volunteerService.createVolunteer(request, entidadId.intValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(voluntario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoluntarioResponseDto> editarVoluntario(
            @RequestBody VoluntarioRequestDto request,
            @RequestParam Long entidadId,
            @PathVariable Integer id) {

        VoluntarioResponseDto voluntario = volunteerService.updateVolunteer(id, request, entidadId.intValue());
        return ResponseEntity.ok(voluntario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVoluntario(
            @RequestParam Long entidadId,
            @PathVariable Integer id) {

        volunteerService.deleteVolunteer(id, entidadId.intValue());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
    }
}