package es.grupo8.backend.controllers;

import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.services.PartnerEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/partner-entities")
public class PartnerEntityController {

    @Autowired
    private PartnerEntityService partnerEntityService;

    @GetMapping
    public ResponseEntity<List<PartnerEntityResponseDto>> getPartnerEntities() {
        List<PartnerEntityResponseDto> entities = partnerEntityService.getAllPartnerEntities();
        return ResponseEntity.ok(entities);
    }
}