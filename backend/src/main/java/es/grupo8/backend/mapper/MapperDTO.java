package es.grupo8.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

public abstract class MapperDTO<DTOClass, EntityClass> {

    public abstract DTOClass toDTO(EntityClass entity);

    public List<DTOClass> toDTOList(List<EntityClass> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}