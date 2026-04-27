package es.grupo8.backend.services;

import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.dto.PartnerEntityRequestDto;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.entity.PartnerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class PartnerEntityService {

    @Autowired
    private PartnerEntityRepository partnerEntityRepository;

    /**
     * Obtiene todas las entidades partner con paginación, ordenamiento y búsqueda.
     */
    public PaginatedResponse<PartnerEntityResponseDto> getAllPartnerEntities(
            int page,
            int size,
            String sort,
            String search) {

        // Validar parámetros
        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100)); // máximo 100 por página

        // Obtener todos y filtrar
        List<PartnerEntity> allEntities = partnerEntityRepository.findAll();

        // Aplicar búsqueda
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            allEntities = allEntities.stream()
                    .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(searchLower))
                    .toList();
        }

        // Aplicar ordenamiento (por defecto por ID si no se especifica)
        if (sort != null && !sort.trim().isEmpty()) {
            allEntities = applySorting(allEntities, sort);
        } else {
            // Ordenar por ID por defecto
            allEntities = allEntities.stream()
                    .sorted(Comparator.comparing(PartnerEntity::getId))
                    .toList();
        }

        // Calcular paginación
        long totalElements = allEntities.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, (int) totalElements);

        // Extraer página
        List<PartnerEntityResponseDto> pageContent;
        if (startIndex >= allEntities.size()) {
            pageContent = List.of();
        } else {
            pageContent = allEntities.subList(startIndex, endIndex)
                    .stream()
                    .map(this::toDto)
                    .toList();
        }

        // Armar respuesta
        return new PaginatedResponse<>(
                pageContent,
                page,
                size,
                totalElements,
                totalPages,
                page < totalPages - 1,  // hasNext
                page > 0                 // hasPrevious
        );
    }

    /**
     * Aplica ordenamiento a la lista.
     * Formato esperado: "name,asc" o "name,desc"
     */
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
            return entities; // Si hay error, devuelve sin ordenamiento
        }
    }

    /**
     * Devuelve un comparador según el campo.
     */
    private Comparator<PartnerEntity> getComparator(String field) {
        return switch (field) {
            case "name" -> Comparator.comparing(e -> e.getName() == null ? "" : e.getName());
            case "phone" -> Comparator.comparing(e -> e.getPhone() == null ? "" : e.getPhone());
            case "address" -> Comparator.comparing(e -> e.getAddress() == null ? "" : e.getAddress());
            case "id" -> Comparator.comparing(PartnerEntity::getId);
            default -> null;
        };
    }

    /**
     * Convierte una entidad JPA a DTO de respuesta.
     */
    private PartnerEntityResponseDto toDto(PartnerEntity entity) {
        return new PartnerEntityResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getPhone()
        );
    }

    public PartnerEntityResponseDto getPartnerEntityById(Integer id) throws RuntimeException {
        PartnerEntity entity = partnerEntityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner entity not found with ID: " + id));
        return toDto(entity);
    }

    public PartnerEntityResponseDto createPartnerEntity(PartnerEntityRequestDto request) throws RuntimeException {
        PartnerEntity entity = new PartnerEntity();
        entity.setName(request.getName());
        entity.setAddress(request.getAddress());
        entity.setPhone(request.getPhone());

        try {
            PartnerEntity savedEntity = partnerEntityRepository.save(entity);
            return toDto(savedEntity);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid data provided for creating partner entity, entity is Null: " + e.getMessage());
        }
    }

    public PartnerEntityResponseDto updatePartnerEntity(Integer id, PartnerEntityRequestDto request) throws RuntimeException {
        PartnerEntity entity = partnerEntityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partner entity not found with ID: " + id));

        entity.setName(request.getName());
        entity.setAddress(request.getAddress());
        entity.setPhone(request.getPhone());

        try {
            PartnerEntity updatedEntity = partnerEntityRepository.save(entity);
            return toDto(updatedEntity);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid data provided for updating partner entity, entity is Null: " + e.getMessage());
        }
    }

    public void deletePartnerEntity(Integer id) throws RuntimeException {
        if (!partnerEntityRepository.existsById(id)) {
            throw new RuntimeException("Partner entity not found with ID: " + id);
        }
        partnerEntityRepository.deleteById(id);
    }
}
