package es.grupo8.backend.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dao.VolunteerRepository;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.entity.Volunteer;
import es.grupo8.backend.security.AdminGuard;


@Controller
@RequestMapping("/api/export")
public class ExportController {
    
    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private CampaignStoreRepository campaignStoreRepository;



    @GetMapping("/{resource}")
    public ResponseEntity<?> export (@PathVariable String resource) {

        try{
            byte[] xlsx = switch (resource) {
                case "stores" -> exportStores();
                case "volunteers" -> exportVolunteers();
                case "campaigns" -> exportCampaigns();
                default -> null;
            };

            if(xlsx == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Recurso no válido. Use 'stores', 'volunteers' o 'campaigns'.");
            }

            String file = resource + "_export.xlsx";
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);


        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al generar el archivo: " + e.getMessage());
        }
    }



    private byte[] exportStores() throws IOException {

    }


    private byte[] exportVolunteers() throws IOException {

    }


    private byte[] exportCampaigns() throws IOException {

    }


}