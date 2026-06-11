package es.grupo8.backend.dto;

import lombok.Data;

@Data
public class StoreRequestDto {

    private String name;
    private String address;
    private String postalCode;
    private Integer chainId;
}
