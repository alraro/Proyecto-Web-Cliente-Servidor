/*
*
* Autores:
*	- Hugo Herrero González: 100%
*/
package es.grupo8.backend.dto;
import lombok.Data;

@Data
public class ExportChainDTO {
    private Integer id;
    private String name;
    private String code;
    private String participation;
}