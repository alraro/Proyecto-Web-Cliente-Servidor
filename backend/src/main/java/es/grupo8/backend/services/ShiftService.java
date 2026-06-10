package es.grupo8.backend.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.ShiftRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.dto.ShiftCalendarDayDto;
import es.grupo8.backend.dto.ShiftCalendarItemDto;
import es.grupo8.backend.dto.ShiftCalendarStoreDto;
import es.grupo8.backend.dto.ShiftRequestDto;
import es.grupo8.backend.dto.ShiftResponseDto;
import es.grupo8.backend.dto.StoreSimpleDto;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Shift;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.mapper.ShiftMapper;

@Service
public class ShiftService {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CampaignStoreRepository campaignStoreRepository;

    @Autowired
    private VolunteerShiftRepository volunteerShiftRepository;

    @Autowired
    private ShiftMapper shiftMapper;

    /**
     * Creates a new pickup shift for a given campaign and store.
     * Validates all fields, date ranges and campaign-store association before persisting.
     *
     * @param userId  id of the coordinator creating the shift
     * @param request DTO with shift creation data
     * @return the created shift as a response DTO
     * @throws IllegalArgumentException if any input field is invalid or the association does not exist
     */
    public ShiftResponseDto createShift(Integer userId, ShiftRequestDto request) {
        validateRequiredFields(request);

        LocalDate day = parseDate(request.getDay());
        LocalTime startTime = parseTime(request.getStartTime());
        LocalTime endTime = parseTime(request.getEndTime());

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }

        if (request.getVolunteersNeeded() <= 0) {
            throw new IllegalArgumentException("El numero de voluntarios debe ser mayor que 0");
        }

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new IllegalArgumentException("La campana no existe"));

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("La tienda no existe"));

        if (day.isBefore(campaign.getStartDate()) || day.isAfter(campaign.getEndDate())) {
            throw new IllegalArgumentException(
                    "El dia debe estar dentro del rango de la campana (" +
                    campaign.getStartDate() + " a " + campaign.getEndDate() + ")");
        }

        if (!campaign.getStores().contains(store)) {
            throw new IllegalArgumentException("La tienda no esta asociada a esta campana");
        }

        Shift shift = new Shift();
        shift.setIdCampaign(campaign);
        shift.setIdStore(store);
        shift.setShiftDay(day);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setVolunteersNeeded(request.getVolunteersNeeded());
        shift.setLocation(request.getLocation());
        shift.setObservations(request.getObservations());
        shift.setCreatedBy(userId);

        return shiftMapper.toDTO(shiftRepository.save(shift));
    }

    /**
     * Returns shifts for a campaign, optionally filtered by store.
     *
     * @param campaignId required campaign identifier
     * @param storeId    optional store identifier for additional filtering
     * @return list of shift response DTOs
     * @throws IllegalArgumentException if campaignId is not provided
     */
    public List<ShiftResponseDto> getShifts(Integer campaignId, Integer storeId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("Proporcione campaignId para obtener los turnos");
        }

        List<Shift> shifts = (storeId != null)
                ? shiftRepository.findByCampaignAndStore(campaignId, storeId)
                : shiftRepository.findByIdCampaign(campaignId);

        return shiftMapper.toDTOList(shifts);
    }

    /**
     * Returns the stores associated to a campaign, sorted alphabetically by name.
     *
     * @param campaignId the campaign identifier
     * @return list of simple store DTOs
     * @throws RuntimeException if the campaign does not exist
     */
    public List<StoreSimpleDto> getStoresForCampaign(Integer campaignId) {
        campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));

        return campaignStoreRepository.findByIdCampaign_Id(campaignId).stream()
                .filter(cs -> cs.getIdStore() != null)
                .map(cs -> {
                    StoreSimpleDto dto = new StoreSimpleDto();
                    dto.setId(cs.getIdStore().getId());
                    dto.setName(cs.getIdStore().getName());
                    return dto;
                })
                .sorted(Comparator.comparing(StoreSimpleDto::getName))
                .collect(Collectors.toList());
    }

    /**
     * Builds the visual calendar data for a campaign, grouping shifts by store → day → time slot.
     * Uses two database queries to minimise load (RNF-06).
     *
     * @param campaignId the campaign identifier
     * @return list of calendar store DTOs ordered chronologically
     * @throws IllegalArgumentException if campaignId is not provided
     */
    public List<ShiftCalendarStoreDto> getCalendar(Integer campaignId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId es obligatorio");
        }

        List<Shift> shifts = shiftRepository.findByIdCampaign(campaignId);

        Map<String, Long> countMap = buildVolunteerCountMap(campaignId);

        Map<Integer, Map<String, Object>> storeMap = new LinkedHashMap<>();

        for (Shift s : shifts) {
            Integer sid   = s.getIdStore().getId();
            String sName  = s.getIdStore().getName();
            String dayKey = s.getShiftDay().toString();
            String cntKey = sid + "|" + dayKey + "|" + s.getStartTime().toString();
            long assigned = countMap.getOrDefault(cntKey, 0L);

            storeMap.computeIfAbsent(sid, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("storeId",   sid);
                m.put("storeName", sName);
                m.put("days",      new LinkedHashMap<String, List<ShiftCalendarItemDto>>());
                return m;
            });

            @SuppressWarnings("unchecked")
            Map<String, List<ShiftCalendarItemDto>> days =
                    (Map<String, List<ShiftCalendarItemDto>>) storeMap.get(sid).get("days");

            ShiftCalendarItemDto item = new ShiftCalendarItemDto();
            item.setShiftId(s.getId());
            item.setStartTime(s.getStartTime().toString());
            item.setEndTime(s.getEndTime().toString());
            item.setVolunteersNeeded(s.getVolunteersNeeded());
            item.setVolunteersAssigned((int) assigned);
            item.setObservations(s.getObservations() != null ? s.getObservations() : "");

            days.computeIfAbsent(dayKey, k -> new ArrayList<>()).add(item);
        }

        return buildCalendarResult(storeMap);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void validateRequiredFields(ShiftRequestDto request) {
        if (request == null ||
                request.getCampaignId() == null ||
                request.getStoreId() == null ||
                request.getDay() == null ||
                request.getStartTime() == null ||
                request.getEndTime() == null ||
                request.getVolunteersNeeded() == null) {
            throw new IllegalArgumentException(
                    "Campos obligatorios: campaignId, storeId, day, startTime, endTime, volunteersNeeded");
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha invalido. Use YYYY-MM-DD");
        }
    }

    private LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de hora invalido. Use HH:mm");
        }
    }

    private Map<String, Long> buildVolunteerCountMap(Integer campaignId) {
        Map<String, Long> countMap = new HashMap<>();
        for (Object[] row : volunteerShiftRepository.countVolunteersPerShiftInCampaign(campaignId)) {
            Integer sid  = (Integer)   row[0];
            LocalDate d  = (LocalDate) row[1];
            LocalTime st = (LocalTime) row[2];
            Long count   = (Long)      row[3];
            countMap.put(sid + "|" + d + "|" + st, count);
        }
        return countMap;
    }

    private List<ShiftCalendarStoreDto> buildCalendarResult(Map<Integer, Map<String, Object>> storeMap) {
        List<ShiftCalendarStoreDto> result = new ArrayList<>();

        for (Map<String, Object> storeEntry : storeMap.values()) {
            @SuppressWarnings("unchecked")
            Map<String, List<ShiftCalendarItemDto>> daysMap =
                    (Map<String, List<ShiftCalendarItemDto>>) storeEntry.get("days");

            List<ShiftCalendarDayDto> dayList = daysMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> {
                        List<ShiftCalendarItemDto> dayShifts = e.getValue().stream()
                                .sorted(Comparator.comparing(ShiftCalendarItemDto::getStartTime))
                                .collect(Collectors.toList());
                        ShiftCalendarDayDto day = new ShiftCalendarDayDto();
                        day.setDate(e.getKey());
                        day.setShifts(dayShifts);
                        return day;
                    })
                    .collect(Collectors.toList());

            ShiftCalendarStoreDto storeDto = new ShiftCalendarStoreDto();
            storeDto.setStoreId((Integer) storeEntry.get("storeId"));
            storeDto.setStoreName((String) storeEntry.get("storeName"));
            storeDto.setDays(dayList);
            result.add(storeDto);
        }

        return result;
    }
}
