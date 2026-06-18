/*
*   Participación en el proyecto:
*  - Hugo Herrero González: 80%
*  - IA Generativa: 20%
*/

package es.grupo8.backend.controllers.rest;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import es.grupo8.backend.services.ExportService;

@RestController
@RequestMapping("/api/export")
public class ExportController extends BaseRestController {

    @Autowired
    private ExportService exportService;


    @GetMapping("/{resource}")
    public ResponseEntity<byte[]> export(@PathVariable String resource) throws IOException{
        // Llamamos al servicio para generar el Excel
        byte[] xlsx = exportService.generateExcelExport(resource);

        if (xlsx == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recurso no válido para exportar: " + resource);
        }

        // Configuramos la respuesta para poder descargar el archivo
        return ResponseEntity.ok()
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource + "_export.xlsx\"")
                             .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                             .body(xlsx);
    }
}