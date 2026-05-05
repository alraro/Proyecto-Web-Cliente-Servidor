/*
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
    public ResponseEntity<?> export(@PathVariable String resource) {

        try {
            byte[] xlsx = switch (resource) {
                case "stores" -> exportStores();
                case "volunteers" -> exportVolunteers();
                case "campaigns" -> exportCampaigns();
                default -> null;
            };

            if (xlsx == null) {
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
        List<Store> stores = storeRepository.findAll();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Tiendas");
            CellStyle headerStyle = createHeaderStyle(wb);

            String[] cols = {"ID", "Nombre", "Dirección", "Codigo Postal", "Localidad", "Zona", "Cadena"};

            createHeaderRow(sheet, cols, headerStyle);

            int rowNum = 1;
            for(Store s : stores){
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(s.getId());
                row.createCell(1).setCellValue(s.getName());
                row.createCell(2).setCellValue(nullSafe(s.getAddress()));

                if(s.getPostalCode() != null){
                    row.createCell(3).setCellValue(s.getPostalCode().getPostalCode());
                    if(s.getPostalCode().getIdLocality() != null){
                        row.createCell(4).setCellValue(s.getPostalCode().getIdLocality().getName());
                        if(s.getPostalCode().getIdLocality().getIdZone() != null){
                            row.createCell(5).setCellValue(s.getPostalCode().getIdLocality().getIdZone().getName());
                        }
                    }
                }

                if(s.getIdChain() != null){
                    row.createCell(6).setCellValue(s.getIdChain().getName());
                }
            }

            autoSizeColumns(sheet, cols.length);
            return toBytes(wb);
        }
    }


    private byte[] exportVolunteers() throws IOException {
        List<Volunteer> volunteers = volunteerRepository.findAll();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Voluntarios");
            CellStyle headerStyle = createHeaderStyle(wb);

        }
    }


    private byte[] exportCampaigns() throws IOException {
        return null;
    }


    private CellStyle createHeaderStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void createHeaderRow(Sheet sheet, String[] cols, CellStyle style) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < cols.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(style);
        }
    }

    private void autoSizeColumns(Sheet sheet, int numCols) {
        for (int i = 0; i < numCols; i++) {
            sheet.autoSizeColumn(i);
            // Ancho mínimo de 12 caracteres para que no queden muy estrechas
            if (sheet.getColumnWidth(i) < 3072) sheet.setColumnWidth(i, 3072);
        }
    }

    private byte[] toBytes(XSSFWorkbook wb) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

}
*/