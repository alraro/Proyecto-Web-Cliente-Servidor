/**
 * Autores:
 * - Alfonso Ramos Rojas: 85%
 * - IA Generativa: 15%
 */
package es.grupo8.backend.services;

import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dto.PartnerEntityResponseDto;
import es.grupo8.backend.dto.PartnerEntityRequestDto;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.entity.PartnerEntity;
import es.grupo8.backend.mapper.PartnerEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

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
        int offset = page * size;

        UtilsService.SortInfo sortInfo = UtilsService.parseSort(sort);

        List<PartnerEntity> entities = switch (sortInfo.field()) {
            case "name" -> sortInfo.order().equals("asc")
                    ? partnerEntityRepository.findAllByNameAsc(search, size, offset)
                    : partnerEntityRepository.findAllByNameDesc(search, size, offset);
            default -> sortInfo.order().equals("asc")
                    ? partnerEntityRepository.findAllByIdAsc(search, size, offset)
                    : partnerEntityRepository.findAllByIdDesc(search, size, offset);
        };

        long total = partnerEntityRepository.countWithSearch(search);
        int totalPages = (int) Math.ceil((double) total / size);

        return new PaginatedResponse<>(
                partnerEntityMapper.toDTOList(entities),
                page,
                size,
                total,
                totalPages,
                page < totalPages - 1,
                page > 0
        );
    }

    public PartnerEntityResponseDto getPartnerEntityById(Integer id) {
        PartnerEntity entity = partnerEntityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Partner entity not found with ID: " + id));
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
                .orElseThrow(() -> new NoSuchElementException("Partner entity not found with ID: " + id));

        entity.setName(request.getName().trim());
        entity.setAddress(UtilsService.trimToNull(request.getAddress()));
        entity.setPhone(UtilsService.normalizePhone(request.getPhone()));

        PartnerEntity updatedEntity = partnerEntityRepository.save(entity);
        return partnerEntityMapper.toDTO(updatedEntity);
    }

    public void deletePartnerEntity(Integer id) {
        if (!partnerEntityRepository.existsById(id)) {
            throw new NoSuchElementException("Partner entity not found with ID: " + id);
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
