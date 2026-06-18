/**
 * Controlador REST de operaciones de turnos del coordinador.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dto.ShiftCalendarStoreDto;
import es.grupo8.backend.dto.ShiftRequestDto;
import es.grupo8.backend.dto.ShiftResponseDto;
import es.grupo8.backend.dto.StoreSimpleDto;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.ShiftService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/shifts")
@AllArgsConstructor
public class CoordinatorRestController extends BaseRestController {

    private final AuthService authService;
    private final UserService userService;
    private final ShiftService shiftService;

    @PostMapping
    public ResponseEntity<?> createShift(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) ShiftRequestDto request) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden("coordinators");
        Integer userId = authService.extractUserIdFromToken(authHeader);
        ShiftResponseDto created = shiftService.createShift(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<?> getShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId", required = false) Integer storeId) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden("coordinators");
        List<ShiftResponseDto> shifts = shiftService.getShifts(campaignId, storeId);
        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/campaign/{campaignId}/stores")
    public ResponseEntity<?> getStoresForCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId) {

        List<StoreSimpleDto> stores = shiftService.getStoresForCampaign(campaignId);
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/calendar")
    public ResponseEntity<?> getCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden("coordinators");
        List<ShiftCalendarStoreDto> calendar = shiftService.getCalendar(campaignId);
        return ResponseEntity.ok(calendar);
    }

}
