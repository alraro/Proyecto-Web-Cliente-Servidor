package es.grupo8.backend.services;

import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.dto.PartnerEntityRequestDto;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.entity.PartnerEntity;
import es.grupo8.backend.mapper.PartnerEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class PartnerEntityService {

    @Autowired
    private PartnerEntityRepository partnerEntityRepository;

    @Autowired
    private PartnerEntityMapper partnerEntityMapper;

    public PaginatedResponse<PartnerEntityResponseDto> getAllPartnerEntities(
            int page,
            int size,
            String sort,
            String search) {

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100));

        List<PartnerEntity> allEntities = partnerEntityRepository.findAll();

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            allEntities = allEntities.stream()
                    .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(searchLower))
                    .toList();
        }

        if (sort != null && !sort.trim().isEmpty()) {
            allEntities = applySorting(allEntities, sort);
        } else {
            allEntities = allEntities.stream()
                    .sorted(Comparator.comparing(PartnerEntity::getId))
                    .toList();
        }

        long totalElements = allEntities.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, (int) totalElements);

        List<PartnerEntityResponseDto> pageContent;
        if (startIndex >= allEntities.size()) {
            pageContent = List.of();
        } else {
            pageContent = partnerEntityMapper.toDTOList(allEntities.subList(startIndex, endIndex));
        }

        return new PaginatedResponse<>(
                pageContent,
                page,
                size,
                totalElements,
                totalPages,
                page < totalPages - 1,
                page > 0
        );
    }

    private List<PartnerEntity> applySorting(List<PartnerEntity> entities, String sort) {
        try {
            String[] parts = sort.split(",");
            if (parts.length != 2) return entities;

            String field = parts[0].trim().toLowerCase();
            String direction = parts[1].trim().toLowerCase();

            Comparator<PartnerEntity> comparator = getComparator(field);
            if (comparator == null) return entities;

            if ("desc".equals(direction)) {
                comparator = comparator.reversed();
            }

            return entities.stream()
                    .sorted(comparator)
                    .toList();
        } catch (Exception e) {
            return entities;
        }
    }

    private Comparator<PartnerEntity> getComparator(String field) {
        return switch (field) {
            case "name" -> Comparator.comparing(e -> e.getName() == null ? "" : e.getName());
            case "phone" -> Comparator.comparing(e -> e.getPhone() == null ? "" : e.getPhone());
            case "address" -> Comparator.comparing(e -> e.getAddress() == null ? "" : e.getAddress());
            case "id" -> Comparator.comparing(PartnerEntity::getId);
            default -> null;
        };
    }

    public PartnerEntityResponseDto getPartnerEntityById(Integer id) {
        PartnerEntity entity = partnerEntityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner entity not found with ID: " + id));
        return partnerEntityMapper.toDTO(entity);
    }

    public PartnerEntityResponseDto createPartnerEntity(PartnerEntityRequestDto request) {
        validateRequest(request);

        PartnerEntity entity = new PartnerEntity();
        entity.setName(request.getName().trim());
        entity.setAddress(UtilsService.trimToNull(request.getAddress()));
        entity.setPhone(UtilsService.normalizePhone(request.getPhone()));

        PartnerEntity savedEntity = partnerEntityRepository.save(entity);
        return partnerEntityMapper.toDTO(savedEntity);
    }

    public PartnerEntityResponseDto updatePartnerEntity(Integer id, PartnerEntityRequestDto request) {
        validateRequest(request);

        PartnerEntity entity = partnerEntityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner entity not found with ID: " + id));

        entity.setName(request.getName().trim());
        entity.setAddress(UtilsService.trimToNull(request.getAddress()));
        entity.setPhone(UtilsService.normalizePhone(request.getPhone()));

        PartnerEntity updatedEntity = partnerEntityRepository.save(entity);
        return partnerEntityMapper.toDTO(updatedEntity);
    }

    public void deletePartnerEntity(Integer id) {
        if (!partnerEntityRepository.existsById(id)) {
            throw new RuntimeException("Partner entity not found with ID: " + id);
        }
        partnerEntityRepository.deleteById(id);
    }

    private void validateRequest(PartnerEntityRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es inválida.");
        }

        String name = request.getName() == null ? "" : request.getName().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("El nombre no puede superar 255 caracteres.");
        }

        String phone = UtilsService.normalizePhone(request.getPhone());
        if (phone != null) {
            if (!UtilsService.PHONE_PATTERN.matcher(phone).matches()) {
                throw new IllegalArgumentException("El teléfono tiene un formato inválido.");
            }

            String digitsOnly = phone.replaceAll("\\D", "");
            if (digitsOnly.length() < 7 || digitsOnly.length() > 15) {
                throw new IllegalArgumentException("El teléfono debe tener entre 7 y 15 dígitos.");
            }
        }
    }
}
