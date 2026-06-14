/**
 * Clase base genérica para los mapeadores entre entidades y DTOs.
 *
 * Autores:
 * - Hugo Herrero González: 60%
 * - Fernando Luis Pinilla Molina: 25%
 * - Alejandra Ortiz: 15%
 */
package es.grupo8.backend.mapper;
import java.util.List;
import java.util.Set;
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

    public Set<DTOClass> toDtoSet(Set<EntityClass> entities) {
        if (entities == null) {
            return Set.of();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toSet());
    }
}