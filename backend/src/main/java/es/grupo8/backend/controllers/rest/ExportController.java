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

import es.grupo8.backend.services.ExportService;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private ExportService exportService;


    @GetMapping("/{resource}")
    public ResponseEntity<?> export(@PathVariable String resource) {
        try {
            // Llamamos al servicio para generar el Excel
            byte[] xlsx = exportService.generateExcelExport(resource);

            if (xlsx == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Recurso no válido");
            }

            // Configuramos la respuesta para poder descargar el archivo
            return ResponseEntity.ok()
                                 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource + "_export.xlsx\"")
                                 .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                                 .body(xlsx);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al generar el archivo: " + e.getMessage());
        }
    }
}