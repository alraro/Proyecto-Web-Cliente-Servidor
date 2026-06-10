package es.grupo8.backend.mapper;

import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dto.UserResponseDto;
import es.grupo8.backend.entity.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class UserMapper extends MapperDTO<UserResponseDto, UserEntity> {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Override
    public UserResponseDto toDTO(UserEntity entity) {
        if (entity == null) return null;

        return new UserResponseDto(
                entity.getIdUser(),
                resolveRoles(entity.getIdUser()),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getAddress(),
                entity.getPostalCode()
        );
    }

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

    public String resolveRole(Integer userId) {
        List<String> roles = resolveRoles(userId);
        return roles.get(0);
    }
}
