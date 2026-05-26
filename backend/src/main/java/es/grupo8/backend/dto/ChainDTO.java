package es.grupo8.backend.dto;

import lombok.Data;

@Data
public class ChainDTO {

    private Integer id;
    private String  name;
    private String  code;
    private Boolean participation;
}