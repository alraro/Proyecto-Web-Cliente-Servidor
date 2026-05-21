package es.grupo8.backend.controllers;

import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.PartnerEntityManagerAssignRequestDto;
import es.grupo8.backend.dto.PartnerEntityManagerResponseDto;
import es.grupo8.backend.dto.PartnerEntityManagerUpdateRequestDto;
import es.grupo8.backend.services.PartnerEntityManagerService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/partner-entity-managers")
public class PartnerEntityManagerController {

	@Autowired
	private PartnerEntityManagerService partnerEntityManagerService;

	@GetMapping
	public ResponseEntity<PaginatedResponse<PartnerEntityManagerResponseDto>> getPartnerEntityManagers(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) String search) {

		PaginatedResponse<PartnerEntityManagerResponseDto> response =
				partnerEntityManagerService.getAllPartnerEntityManagers(page, size, sort, search);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<PartnerEntityManagerResponseDto> getPartnerEntityManagerByUserId(@PathVariable Integer userId) {
		PartnerEntityManagerResponseDto response = partnerEntityManagerService.getPartnerEntityManagerByUserId(userId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{userId}")
	public ResponseEntity<PartnerEntityManagerResponseDto> promoteUserToPartnerEntityManager(
			@PathVariable Integer userId,
			@RequestBody(required = false) PartnerEntityManagerAssignRequestDto request) {

		PartnerEntityManagerResponseDto response =
				partnerEntityManagerService.promoteUserToPartnerEntityManager(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{userId}")
	public ResponseEntity<PartnerEntityManagerResponseDto> updatePartnerEntityManager(
			@PathVariable Integer userId,
			@RequestBody PartnerEntityManagerUpdateRequestDto request) {

		PartnerEntityManagerResponseDto response =
				partnerEntityManagerService.updatePartnerEntityManager(userId, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> removePartnerEntityManagerRole(@PathVariable Integer userId) {
		partnerEntityManagerService.removePartnerEntityManagerRole(userId);
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
