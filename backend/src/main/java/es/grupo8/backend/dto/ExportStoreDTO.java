/*
*
* Autores:
*	- Hugo Herrero González: 100%
*/
package es.grupo8.backend.dto;
import lombok.Data;

@Data
public class ExportStoreDTO {
    private Integer id;
    private String name;
    private String address;
    private String locality;
    private String postalCode;
    private String zone;
    private String chain;
}