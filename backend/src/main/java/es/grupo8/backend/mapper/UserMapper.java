/**
 * Mapeador entre UserDTO y UserEntity.
 *
 * Autores:
 * - Alejandra Ortiz: 70%
 * - Alfonso Ramos: 30%
 */
package es.grupo8.backend.mapper;
 
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dto.UserDTO;
import es.grupo8.backend.entity.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
 
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class UserMapper extends MapperDTO<UserDTO, UserEntity> {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Override
    public UserDTO toDTO(UserEntity entity) {
        UserDTO dto = new UserDTO();
        dto.setIdUser(entity.getIdUser());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setPostalCode(entity.getPostalCode());
        dto.setRoles(resolveRoles(entity.getIdUser()));
        // password nunca se mapea a la respuesta
        return dto;
    }

    /**
     * Devuelve todos los roles del usuario como lista.
     * Si no tiene ninguno → ["PENDIENTE"].
     */
    public List<String> resolveRoles(Integer userId) {
        List<String> roles = new ArrayList<>();
        if (userRepository.isAdmin(userId)) roles.add("ADMINISTRADOR");
        if (userRepository.isCoordinator(userId)) roles.add("COORDINADOR");
        if (userRepository.isCaptain(userId)) roles.add("CAPITAN");
        if (userRepository.isPartnerEntityManager(userId)) roles.add("COLABORADOR");
        if (storeRepository.existsByIdResponsible_IdUser(userId)) roles.add("RESPONSABLE_TIENDA");
        if (roles.isEmpty()) roles.add("PENDIENTE");
        return roles;
    }

    /**
     * Devuelve el rol principal (el primero) como String simple.
     * Útil para el filtrado de pendientes.
     */
    public String resolveRole(Integer userId) {
        List<String> roles = resolveRoles(userId);
        return roles.get(0);
    }
}