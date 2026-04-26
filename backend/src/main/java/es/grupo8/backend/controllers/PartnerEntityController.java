package es.grupo8.backend.controllers;

import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.services.PartnerEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import es.grupo8.backend.dto.PartnerEntityRequestDto;

@RestController
@RequestMapping("/api/partner-entities")
public class PartnerEntityController {

	@Autowired
	private PartnerEntityService partnerEntityService;

	/**
	 * GET /api/partner-entities?page=0&size=20&sort=name,asc&search=banco
	 *
	 * Query params:
	 * - page: página (default 0)
	 * - size: elementos por página (default 20, máximo 100)
	 * - sort: ordenamiento "field,direction" ej: "name,asc" (opcional)
	 * - search: búsqueda por nombre (opcional)
	 */
	@GetMapping
	public ResponseEntity<PaginatedResponse<PartnerEntityResponseDto>> getPartnerEntities(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) String search) {

		PaginatedResponse<PartnerEntityResponseDto> response =
				partnerEntityService.getAllPartnerEntities(page, size, sort, search);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<PartnerEntityResponseDto> getPartnerEntityById(
		@PathVariable Integer id) {
		System.out.println("Received request GET for partner entity with ID: " + id);
		
		PartnerEntityResponseDto response = partnerEntityService.getPartnerEntityById(id);
		System.out.println("Returning response successfully (200 OK) for partner entity with ID: " + id);
		return ResponseEntity.ok(response);
	}

	@PostMapping
	public ResponseEntity<PartnerEntityResponseDto> createPartnerEntity(
		@RequestBody PartnerEntityRequestDto request) {
		System.out.println("Received request POST to create partner entity with name: " + request.getName());
		PartnerEntityResponseDto response = partnerEntityService.createPartnerEntity(request);
		System.out.println("Partner entity created successfully with ID: " + response.id());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<PartnerEntityResponseDto> updatePartnerEntity(
		@PathVariable Integer id,
		@RequestBody PartnerEntityRequestDto request) {
		System.out.println("Received request PUT to update partner entity with ID: " + id);
		PartnerEntityResponseDto response = partnerEntityService.updatePartnerEntity(id, request);
		System.out.println("Partner entity updated successfully with ID: " + id);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePartnerEntity(
		@PathVariable Integer id) {
		System.out.println("Received request DELETE to remove partner entity with ID: " + id);
		partnerEntityService.deletePartnerEntity(id);
		System.out.println("Partner entity deleted successfully with ID: " + id);
		return ResponseEntity.noContent().build();
	}
}
