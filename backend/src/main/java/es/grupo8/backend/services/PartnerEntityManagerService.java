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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PartnerEntityManagerService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9()\\-\\s]{7,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Autowired
    private PartnerEntityManagerRepository partnerEntityManagerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PartnerEntityRepository partnerEntityRepository;

    @Autowired
    private PostalCodeRepository postalCodeRepository;

    public PaginatedResponse<PartnerEntityManagerResponseDto> getAllPartnerEntityManagers(
            int page,
            int size,
            String sort,
            String search) {

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100));

        List<PartnerEntityManager> allManagers = partnerEntityManagerRepository.findAllWithRelations();

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            allManagers = allManagers.stream()
                    .filter(m -> matchesSearch(m, searchLower))
                    .toList();
        }

        if (sort != null && !sort.trim().isEmpty()) {
            allManagers = applySorting(allManagers, sort);
        } else {
            allManagers = allManagers.stream()
                    .sorted(Comparator.comparing(PartnerEntityManager::getId))
                    .toList();
        }

        long totalElements = allManagers.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, (int) totalElements);

        List<PartnerEntityManagerResponseDto> pageContent;
        if (startIndex >= allManagers.size()) {
            pageContent = List.of();
        } else {
            pageContent = allManagers.subList(startIndex, endIndex)
                    .stream()
                    .map(this::toDto)
                    .toList();
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

    public PartnerEntityManagerResponseDto getPartnerEntityManagerByUserId(Integer userId) {
        PartnerEntityManager manager = partnerEntityManagerRepository.findByIdWithRelations(userId)
                .orElseThrow(() -> new RuntimeException("No existe un responsable de entidad colaboradora con ID de usuario: " + userId));
        return toDto(manager);
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
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        PartnerEntityManager manager = new PartnerEntityManager();
        manager.setId(user.getIdUser());
        manager.setUserAccounts(user);
        manager.setIdPartnerEntity(resolvePartnerEntity(request != null ? request.getPartnerEntityId() : null));

        PartnerEntityManager saved = partnerEntityManagerRepository.save(manager);
        return toDto(saved);
    }

    public PartnerEntityManagerResponseDto updatePartnerEntityManager(
            Integer userId,
            PartnerEntityManagerUpdateRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException("La solicitud es inválida.");
        }

        PartnerEntityManager manager = partnerEntityManagerRepository.findByIdWithRelations(userId)
                .orElseThrow(() -> new RuntimeException("No existe un responsable de entidad colaboradora con ID de usuario: " + userId));

        UserEntity user = manager.getUserAccounts();
        if (user == null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
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

        String normalizedPhone = normalizePhone(request.getPhone());
        if (normalizedPhone != null) {
            if (!PHONE_PATTERN.matcher(normalizedPhone).matches()) {
                throw new IllegalArgumentException("El teléfono tiene un formato inválido.");
            }

            String digitsOnly = normalizedPhone.replaceAll("\\D", "");
            if (digitsOnly.length() < 7 || digitsOnly.length() > 15) {
                throw new IllegalArgumentException("El teléfono debe tener entre 7 y 15 dígitos.");
            }
        }

        String address = trimToNull(request.getAddress());
        if (address != null && address.length() > 1000) {
            throw new IllegalArgumentException("La dirección no puede superar 1000 caracteres.");
        }

        String postalCode = trimToNull(request.getPostalCode());
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

        PartnerEntityManager updated = partnerEntityManagerRepository.findByIdWithRelations(userId)
                .orElse(manager);
        return toDto(updated);
    }

    public void removePartnerEntityManagerRole(Integer userId) {
        if (!partnerEntityManagerRepository.existsById(userId)) {
            throw new RuntimeException("No existe un responsable de entidad colaboradora con ID de usuario: " + userId);
        }
        partnerEntityManagerRepository.deleteById(userId);
    }

    private boolean matchesSearch(PartnerEntityManager manager, String searchLower) {
        UserEntity user = manager.getUserAccounts();
        PartnerEntity partnerEntity = manager.getIdPartnerEntity();

        return String.valueOf(manager.getId()).contains(searchLower)
                || containsIgnoreCase(user != null ? user.getName() : null, searchLower)
                || containsIgnoreCase(user != null ? user.getEmail() : null, searchLower)
                || containsIgnoreCase(user != null ? user.getPhone() : null, searchLower)
                || containsIgnoreCase(user != null ? user.getAddress() : null, searchLower)
                || containsIgnoreCase(user != null ? user.getPostalCode() : null, searchLower)
                || containsIgnoreCase(partnerEntity != null ? partnerEntity.getName() : null, searchLower);
    }

    private List<PartnerEntityManager> applySorting(List<PartnerEntityManager> managers, String sort) {
        try {
            String[] parts = sort.split(",");
            if (parts.length != 2) return managers;

            String field = parts[0].trim().toLowerCase();
            String direction = parts[1].trim().toLowerCase();

            Comparator<PartnerEntityManager> comparator = getComparator(field);
            if (comparator == null) return managers;

            if ("desc".equals(direction)) {
                comparator = comparator.reversed();
            }

            return managers.stream()
                    .sorted(comparator)
                    .toList();
        } catch (Exception e) {
            return managers;
        }
    }

    private Comparator<PartnerEntityManager> getComparator(String field) {
        return switch (field) {
            case "id", "userid" -> Comparator.comparing(PartnerEntityManager::getId, Comparator.nullsLast(Integer::compareTo));
            case "name" -> Comparator.comparing(m -> lowerValue(m.getUserAccounts() != null ? m.getUserAccounts().getName() : null));
            case "email" -> Comparator.comparing(m -> lowerValue(m.getUserAccounts() != null ? m.getUserAccounts().getEmail() : null));
            case "phone" -> Comparator.comparing(m -> lowerValue(m.getUserAccounts() != null ? m.getUserAccounts().getPhone() : null));
            case "address" -> Comparator.comparing(m -> lowerValue(m.getUserAccounts() != null ? m.getUserAccounts().getAddress() : null));
            case "postalcode" -> Comparator.comparing(m -> lowerValue(m.getUserAccounts() != null ? m.getUserAccounts().getPostalCode() : null));
            case "partnerentity", "partnerentityname" -> Comparator.comparing(m -> lowerValue(m.getIdPartnerEntity() != null ? m.getIdPartnerEntity().getName() : null));
            default -> null;
        };
    }

    private PartnerEntity resolvePartnerEntity(Integer partnerEntityId) {
        if (partnerEntityId == null) {
            return null;
        }
        return partnerEntityRepository.findById(partnerEntityId)
                .orElseThrow(() -> new IllegalArgumentException("No existe entidad socia con ID: " + partnerEntityId));
    }

    private PartnerEntityManagerResponseDto toDto(PartnerEntityManager manager) {
        UserEntity user = manager.getUserAccounts();
        PartnerEntity partnerEntity = manager.getIdPartnerEntity();

        return new PartnerEntityManagerResponseDto(
                manager.getId(),
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getPhone() : null,
                user != null ? user.getAddress() : null,
                user != null ? user.getPostalCode() : null,
                partnerEntity != null ? partnerEntity.getId() : null,
                partnerEntity != null ? partnerEntity.getName() : null
        );
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizePhone(String phone) {
        String trimmed = trimToNull(phone);
        if (trimmed == null) {
            return null;
        }
        return trimmed.replaceAll("\\s+", " ");
    }

    private String lowerValue(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private boolean containsIgnoreCase(String value, String searchLower) {
        return value != null && value.toLowerCase().contains(searchLower);
    }
}
