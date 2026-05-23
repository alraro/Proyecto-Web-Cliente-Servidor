package es.grupo8.backend.mapper;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.ExportStoreDTO;
import es.grupo8.backend.entity.Store;

@Component
public class ExportStoreMapper extends MapperDTO<ExportStoreDTO, Store> {

    @Override
    public ExportStoreDTO toDTO(Store s) {
        if(s == null) return null;
        
        ExportStoreDTO dto = new ExportStoreDTO();

        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setAddress(nullSafe(s.getAddress()));
        dto.setLocality(s.getPostalCode().getIdLocality() != null ? nullSafe(s.getPostalCode().getIdLocality().getName()) : "");
        dto.setPostalCode(s.getPostalCode() != null ? nullSafe(s.getPostalCode().getPostalCode()) : "");
        dto.setZone(s.getPostalCode().getIdLocality() != null && s.getPostalCode().getIdLocality().getIdZone() != null ? nullSafe(s.getPostalCode().getIdLocality().getIdZone().getName()) : "");
        dto.setChain(s.getIdChain() != null ? nullSafe(s.getIdChain().getName()) : "");

        return dto;
    }
        private String nullSafe(String value) {
            return value != null ? value : "";
        }   

}