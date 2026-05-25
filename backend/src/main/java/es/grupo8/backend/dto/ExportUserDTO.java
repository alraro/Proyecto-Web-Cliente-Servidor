package es.grupo8.backend.dto;
import lombok.Data;

@Data
public class ExportUserDTO {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String postalCode;
}