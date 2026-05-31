/**
 * Servicio de negocio para operaciones de usuarios.
 *
 * Autores:
 * - Alfonso Ramos: 60%
 * - Alejandra Ortiz: 40%
 */
package es.grupo8.backend.services;

import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dao.UserSpecifications;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.UserDTO;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordService passwordService;
    @Autowired private UserMapper userMapper;

    public PaginatedResponse<UserDTO> getAllUsers(
            int page, int size, String sort, String search, String role) {

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100));

        Specification<UserEntity> spec = Specification
                .where(UserSpecifications.hasSearchTerm(search))
                .and(UserSpecifications.hasRole(role));

        Sort sortObj = Sort.unsorted();
        if (sort != null && !sort.trim().isEmpty()) {
            String[] parts = sort.split(",");
            if (parts.length == 2) {
                Sort.Direction dir = "desc".equalsIgnoreCase(parts[1].trim())
                        ? Sort.Direction.DESC : Sort.Direction.ASC;
                sortObj = Sort.by(dir, "name".equalsIgnoreCase(parts[0].trim()) ? "name" : "idUser");
            }
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<UserEntity> userPage = userRepository.findAll(spec, pageable);

        List<UserDTO> content = userPage.getContent().stream()
                .map(userMapper::toDTO)
                .toList();

        return new PaginatedResponse<>(content, page, size,
                userPage.getTotalElements(), userPage.getTotalPages(),
                userPage.hasNext(), userPage.hasPrevious());
    }

    public UserDTO getUserById(Integer userId) {
        return userMapper.toDTO(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId)));
    }

    public UserDTO createUser(UserDTO request) {
        validateCreate(request);

        if (userRepository.existsByEmail(request.getEmail().toLowerCase()))
            throw new IllegalArgumentException("Ya existe un usuario con ese email.");

        UserEntity user = new UserEntity();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        user.setPostalCode(request.getPostalCode() != null ? request.getPostalCode().trim() : null);
        user.setPassword(passwordService.hash(request.getPassword()));

        UserEntity saved = userRepository.save(user);
        return userMapper.toDTO(userRepository.findById(saved.getIdUser())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado tras la creación")));
    }

    public UserDTO updateUser(Integer userId, UserDTO request) {
        if (request == null) throw new IllegalArgumentException("La petición no es válida.");

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio.");
            if (name.length() > 255) throw new IllegalArgumentException("El nombre no puede superar 255 caracteres.");
            user.setName(name);
        }
        if (request.getEmail() != null) {
            String email = request.getEmail().trim().toLowerCase();
            if (email.isEmpty()) throw new IllegalArgumentException("El email es obligatorio.");
            if (email.length() > 255) throw new IllegalArgumentException("El email no puede superar 255 caracteres.");
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email))
                throw new IllegalArgumentException("Ya existe un usuario con ese email.");
            user.setEmail(email);
        }
        if (request.getPhone() != null) {
            String phone = request.getPhone().trim();
            if (phone.length() > 20) throw new IllegalArgumentException("El teléfono no puede superar 20 caracteres.");
            user.setPhone(phone.isEmpty() ? null : phone);
        }
        if (request.getAddress() != null) {
            String address = request.getAddress().trim();
            user.setAddress(address.isEmpty() ? null : address);
        }
        if (request.getPostalCode() != null) {
            String cp = request.getPostalCode().trim();
            user.setPostalCode(cp.isEmpty() ? null : cp);
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty())
            user.setPassword(passwordService.hash(request.getPassword().trim()));

        return userMapper.toDTO(userRepository.save(user));
    }

    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId))
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        userRepository.deleteById(userId);
    }

    private void validateCreate(UserDTO req) {
        if (req == null) throw new IllegalArgumentException("La petición no es válida.");

        String name = req.getName() != null ? req.getName().trim() : "";
        if (name.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (name.length() > 255) throw new IllegalArgumentException("El nombre no puede superar 255 caracteres.");

        String email = req.getEmail() != null ? req.getEmail().trim().toLowerCase() : "";
        if (email.isEmpty()) throw new IllegalArgumentException("El email es obligatorio.");
        if (email.length() > 255) throw new IllegalArgumentException("El email no puede superar 255 caracteres.");

        if (req.getPassword() == null || req.getPassword().trim().isEmpty())
            throw new IllegalArgumentException("La contraseña es obligatoria.");

        if (req.getRole() == null || req.getRole().trim().isEmpty())
            throw new IllegalArgumentException("El rol es obligatorio.");

        String role = req.getRole().trim().toUpperCase();
        if (!List.of("ADMIN", "COORDINATOR", "CAPTAIN", "PARTNER_ENTITY_MANAGER").contains(role))
            throw new IllegalArgumentException("Rol no válido. Los roles válidos son: ADMIN, COORDINATOR, CAPTAIN, PARTNER_ENTITY_MANAGER.");
    }
}