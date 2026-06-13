package es.grupo8.backend.services;

import es.grupo8.backend.dao.PartnerEntityManagerRepository;
import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dao.PostalCodeRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.PartnerEntityManagerAssignRequestDto;
import es.grupo8.backend.dto.PartnerEntityManagerResponseDto;
import es.grupo8.backend.dto.PartnerEntityManagerUpdateRequestDto;
import es.grupo8.backend.entity.PartnerEntity;
import es.grupo8.backend.entity.PartnerEntityManager;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.mapper.PartnerEntityManagerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

@Service
public class PartnerEntityManagerService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Autowired
    private PartnerEntityManagerRepository partnerEntityManagerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PartnerEntityRepository partnerEntityRepository;

    @Autowired
    private PostalCodeRepository postalCodeRepository;

    @Autowired
    private PartnerEntityManagerMapper partnerEntityManagerMapper;

    public PaginatedResponse<PartnerEntityManagerResponseDto> getAllPartnerEntityManagers(
            int page,
            int size,
            String sort,
            String search) {

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100));
        int offset = page * size;

        UtilsService.SortInfo sortInfo = UtilsService.parseSort(sort);

        List<PartnerEntityManager> managers = switch (sortInfo.field()) {
            case "name" -> sortInfo.order().equals("asc")
                    ? partnerEntityManagerRepository.findAllByNameAsc(search, size, offset)
                    : partnerEntityManagerRepository.findAllByNameDesc(search, size, offset);
            default -> sortInfo.order().equals("asc")
                    ? partnerEntityManagerRepository.findAllByIdAsc(search, size, offset)
                    : partnerEntityManagerRepository.findAllByIdDesc(search, size, offset);
        };

        long total = partnerEntityManagerRepository.countWithSearch(search);
        int totalPages = (int) Math.ceil((double) total / size);

        return new PaginatedResponse<>(
                partnerEntityManagerMapper.toDTOList(managers),
                page,
                size,
                total,
                totalPages,
                page < totalPages - 1,
                page > 0
        );
    }

    public PartnerEntityManagerResponseDto getPartnerEntityManagerByUserId(Integer userId) {
        PartnerEntityManager manager = partnerEntityManagerRepository.findByIdWithRelations(userId)
                .orElseThrow(() -> new NoSuchElementException("No existe un responsable de entidad colaboradora con ID de usuario: " + userId));
        return partnerEntityManagerMapper.toDTO(manager);
    }

    public PartnerEntityManagerResponseDto promoteUserToPartnerEntityManager(
            Integer userId,
            PartnerEntityManagerAssignRequestDto request) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("El ID de usuario es obligatorio.");
        }

        if (partnerEntityManagerRepository.existsById(userId)) {
            throw new IllegalArgumentException("El usuario ya es responsable de entidad colaboradora.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + userId));

        PartnerEntityManager manager = new PartnerEntityManager();
        manager.setId(user.getIdUser());
        manager.setUserAccounts(user);
        manager.setIdPartnerEntity(resolvePartnerEntity(request != null ? request.getPartnerEntityId() : null));

        PartnerEntityManager saved = partnerEntityManagerRepository.save(manager);
        return partnerEntityManagerMapper.toDTO(saved);
    }

    public PartnerEntityManagerResponseDto updatePartnerEntityManager(
            Integer userId,
            PartnerEntityManagerUpdateRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException("La solicitud es inválida.");
        }

        PartnerEntityManager manager = partnerEntityManagerRepository.findByIdWithRelations(userId)
                .orElseThrow(() -> new NoSuchElementException("No existe un responsable de entidad colaboradora con ID de usuario: " + userId));

        UserEntity user = manager.getUserAccounts();
        if (user == null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con ID: " + userId));
            manager.setUserAccounts(user);
        }

        String name = request.getName() == null ? "" : request.getName().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("El nombre no puede superar 255 caracteres.");
        }

        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        if (email.isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio.");
        }
        if (email.length() > 255 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El email tiene un formato inválido.");
        }

        if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe otro usuario con ese email.");
        }

        String normalizedPhone = UtilsService.normalizePhone(request.getPhone());
        if (normalizedPhone != null) {
            if (!UtilsService.PHONE_PATTERN.matcher(normalizedPhone).matches()) {
                throw new IllegalArgumentException("El teléfono tiene un formato inválido.");
            }

            String digitsOnly = normalizedPhone.replaceAll("\\D", "");
            if (digitsOnly.length() < 7 || digitsOnly.length() > 15) {
                throw new IllegalArgumentException("El teléfono debe tener entre 7 y 15 dígitos.");
            }
        }

        String address = UtilsService.trimToNull(request.getAddress());
        if (address != null && address.length() > 1000) {
            throw new IllegalArgumentException("La dirección no puede superar 1000 caracteres.");
        }

        String postalCode = UtilsService.trimToNull(request.getPostalCode());
        if (postalCode != null) {
            if (postalCode.length() > 10) {
                throw new IllegalArgumentException("El código postal no puede superar 10 caracteres.");
            }
            if (!postalCodeRepository.existsById(postalCode)) {
                throw new IllegalArgumentException("El código postal no existe.");
            }
        }

        user.setName(name);
        user.setEmail(email);
        user.setPhone(normalizedPhone);
        user.setAddress(address);
        user.setPostalCode(postalCode);

        manager.setIdPartnerEntity(resolvePartnerEntity(request.getPartnerEntityId()));

        userRepository.save(user);
        partnerEntityManagerRepository.save(manager);

        return partnerEntityManagerMapper.toDTO(manager);
    }

    public void removePartnerEntityManagerRole(Integer userId) {
        if (!partnerEntityManagerRepository.existsById(userId)) {
            throw new NoSuchElementException("No existe un responsable de entidad colaboradora con ID de usuario: " + userId);
        }
        partnerEntityManagerRepository.deleteById(userId);
    }

    private PartnerEntity resolvePartnerEntity(Integer partnerEntityId) {
        if (partnerEntityId == null) {
            return null;
        }
        return partnerEntityRepository.findById(partnerEntityId)
                .orElseThrow(() -> new IllegalArgumentException("No existe entidad socia con ID: " + partnerEntityId));
    }
}
