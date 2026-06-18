/**
 * Autores:
 * - Alejandro Calvo Aguilar: 95%
 * - IA Generativa: 5%
 */

package es.grupo8.backend.mapper;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.CaptainAssignmentDto;
import es.grupo8.backend.entity.ShiftCaptain;

@Component
public class CaptainAssignmentMapper extends MapperDTO<CaptainAssignmentDto, ShiftCaptain> {

    @Override
    public CaptainAssignmentDto toDTO(ShiftCaptain sc) {
        if (sc == null) return null;

        CaptainAssignmentDto dto = new CaptainAssignmentDto();
        dto.setUserId(sc.getUser().getIdUser());
        dto.setName(sc.getUser().getName());
        dto.setEmail(sc.getUser().getEmail());
        return dto;
    }
}
