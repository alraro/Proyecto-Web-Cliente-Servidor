package es.grupo8.backend.services;

import es.grupo8.backend.dao.AdminRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dao.PartnerEntityManagerRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dao.UserSpecifications;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.UserRequestDto;
import es.grupo8.backend.dto.UserResponseDto;
import es.grupo8.backend.dto.UserUpdateRequestDto;
import es.grupo8.backend.entity.AdminEntity;
import es.grupo8.backend.entity.PartnerEntityManager;
import es.grupo8.backend.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CoordinatorRepository coordinatorRepository;

    @Autowired
    private CaptainRepository captainRepository;

    @Autowired
    private PartnerEntityManagerRepository partnerEntityManagerRepository;

    public PaginatedResponse<UserResponseDto> getAllUsers(
            int page,
            int size,
            String sort,
            String search,
            String role) {

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100));

        Specification<UserEntity> spec = Specification.where(UserSpecifications.hasSearchTerm(search))
                .and(UserSpecifications.hasRole(role));

        Sort sortObj = Sort.unsorted();
        if (sort != null && !sort.trim().isEmpty()) {
            String[] parts = sort.split(",");
            if (parts.length == 2) {
                String field = parts[0].trim();
                String direction = parts[1].trim();
                Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
                if ("name".equalsIgnoreCase(field)) {
                    sortObj = Sort.by(dir, "name");
                } else {
                    sortObj = Sort.by(dir, "idUser");
                }
            }
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<UserEntity> userPage = userRepository.findAll(spec, pageable);

        List<UserResponseDto> content = userPage.getContent().stream()
                .map(this::toDto)
                .toList();

        return new PaginatedResponse<>(
                content,
                page,
                size,
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.hasNext(),
                userPage.hasPrevious()
        );
    }

    public UserResponseDto getUserById(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return toDto(user);
    }

    public UserResponseDto createUser(UserRequestDto request) {
        validateUserRequest(request, false);

        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new IllegalArgumentException("A user with that email already exists.");
        }

        UserEntity user = new UserEntity();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPhone(request.phone() != null ? request.phone().trim() : null);
        user.setAddress(request.address() != null ? request.address().trim() : null);
        user.setPostalCode(request.postalCode() != null ? request.postalCode().trim() : null);
        user.setPassword(passwordService.hash(request.password()));

        UserEntity saved = userRepository.save(user);

        // Assign role
        assignRole(saved.getIdUser(), request.role().trim().toUpperCase());

        UserEntity updated = userRepository.findById(saved.getIdUser())
                .orElseThrow(() -> new RuntimeException("User not found after creation"));
        return toDto(updated);
    }

    private void assignRole(Integer userId, String role) {
        switch (role) {
            case "ADMIN" -> {
                AdminEntity admin = new AdminEntity();
                admin.setIdUser(userId);
                adminRepository.save(admin);
            }
            case "COORDINATOR" -> {
                throw new IllegalArgumentException("Coordinator role requires campaign assignment. Use the coordinator endpoint instead.");
            }
            case "CAPTAIN" -> {
                throw new IllegalArgumentException("Captain role requires campaign assignment. Use the captain endpoint instead.");
            }
            case "PARTNER_ENTITY_MANAGER" -> {
                PartnerEntityManager pem = new PartnerEntityManager();
                pem.setId(userId);
                pem.setUserAccounts(userRepository.findById(userId).orElse(null));
                partnerEntityManagerRepository.save(pem);
            }
        }
    }

    public UserResponseDto updateUser(Integer userId, UserUpdateRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("The request is invalid.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (request.name() != null) {
            String name = request.name().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("The name is required.");
            }
            if (name.length() > 255) {
                throw new IllegalArgumentException("The name cannot exceed 255 characters.");
            }
            user.setName(name);
        }

        if (request.email() != null) {
            String email = request.email().trim().toLowerCase();
            if (email.isEmpty()) {
                throw new IllegalArgumentException("The email is required.");
            }
            if (email.length() > 255) {
                throw new IllegalArgumentException("The email cannot exceed 255 characters.");
            }
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("A user with that email already exists.");
            }
            user.setEmail(email);
        }

        if (request.phone() != null) {
            String phone = request.phone().trim();
            if (phone.length() > 20) {
                throw new IllegalArgumentException("The phone cannot exceed 20 characters.");
            }
            user.setPhone(phone.isEmpty() ? null : phone);
        }

        if (request.address() != null) {
            String address = request.address().trim();
            user.setAddress(address.isEmpty() ? null : address);
        }

        if (request.postalCode() != null) {
            String postalCode = request.postalCode().trim();
            user.setPostalCode(postalCode.isEmpty() ? null : postalCode);
        }

        if (request.password() != null && !request.password().trim().isEmpty()) {
            user.setPassword(passwordService.hash(request.password().trim()));
        }

        UserEntity saved = userRepository.save(user);
        return toDto(saved);
    }

    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    private UserResponseDto toDto(UserEntity user) {
        List<String> roles = new ArrayList<>();
        Integer userId = user.getIdUser();

        if (userRepository.isAdmin(userId)) {
            roles.add("ADMIN");
        }
        if (userRepository.isCoordinator(userId)) {
            roles.add("COORDINATOR");
        }
        if (userRepository.isCaptain(userId)) {
            roles.add("CAPTAIN");
        }
        if (userRepository.isPartnerEntityManager(userId)) {
            roles.add("PARTNER_ENTITY_MANAGER");
        }

        return new UserResponseDto(
                user.getIdUser(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getPostalCode(),
                roles
        );
    }

    private void validateUserRequest(UserRequestDto request, boolean isUpdate) {
        if (request == null) {
            throw new IllegalArgumentException("The request is invalid.");
        }

        String name = request.name() != null ? request.name().trim() : "";
        if (name.isEmpty()) {
            throw new IllegalArgumentException("The name is required.");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("The name cannot exceed 255 characters.");
        }

        String email = request.email() != null ? request.email().trim().toLowerCase() : "";
        if (email.isEmpty()) {
            throw new IllegalArgumentException("The email is required.");
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("The email cannot exceed 255 characters.");
        }

        if (request.password() == null || request.password().trim().isEmpty()) {
            throw new IllegalArgumentException("The password is required.");
        }

        if (request.role() == null || request.role().trim().isEmpty()) {
            throw new IllegalArgumentException("The role is required.");
        }

        String role = request.role().trim().toUpperCase();
        if (!List.of("ADMIN", "COORDINATOR", "CAPTAIN", "PARTNER_ENTITY_MANAGER").contains(role)) {
            throw new IllegalArgumentException("Invalid role. Valid roles are: ADMIN, COORDINATOR, CAPTAIN, PARTNER_ENTITY_MANAGER.");
        }
    }
}
