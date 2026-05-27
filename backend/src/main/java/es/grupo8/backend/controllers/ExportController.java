/*
*   Participación en el proyecto:
*  - Hugo Herrero González: 80%
*  - IA Generativa: 20%
*/



package es.grupo8.backend.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import es.grupo8.backend.dto.*;
import es.grupo8.backend.services.ExportService;

@Controller
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private ExportService exportService;


    @GetMapping("/{resource}")
    public ResponseEntity<?> export(
            @PathVariable String resource) {
        try {
            byte[] xlsx = switch (resource) {
                case "stores"    -> exportStores();
                case "chains"    -> exportChains();
                case "campaigns" -> exportCampaigns();
                case "partner-entities", "partners" -> exportPartners();
                case "users"     -> exportUsers();
                default          -> null;
            };

            if (xlsx == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Recurso no válido");
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource + "_export.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(xlsx);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar el archivo: " + e.getMessage());
        }
    }

    // Tiendas
    private byte[] exportStores() throws IOException {
        List<ExportStoreDTO> stores = exportService.getStores();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Tiendas");
            CellStyle hs = createHeaderStyle(wb);

            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Domicilio", "Localidad", "CP", "Zona", "Cadena"
            }, hs);

            int r = 1;
            for (ExportStoreDTO s : stores) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(s.getId()); // ID
                row.createCell(1).setCellValue(s.getName()); // Nombre
                row.createCell(2).setCellValue(s.getAddress()); // Domicilio
                row.createCell(3).setCellValue(s.getLocality()); // Localidad
                row.createCell(4).setCellValue(s.getPostalCode()); // CP
                row.createCell(5).setCellValue(s.getZone()); // Zona
                row.createCell(6).setCellValue(s.getChain()); // Cadena
            }
            autoSizeColumns(sheet, 7);
            return toBytes(wb);
        }
    }

    // Cadenas
    private byte[] exportChains() throws IOException {
        List<ExportChainDTO> chains = exportService.getChains();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Cadenas");
            CellStyle hs = createHeaderStyle(wb);

            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Código", "Participa"
            }, hs);

            int r = 1;
            for (ExportChainDTO c : chains) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(c.getId()); // ID
                row.createCell(1).setCellValue(c.getName()); // Nombre
                row.createCell(2).setCellValue(c.getCode()); // Código
                row.createCell(3).setCellValue(c.getParticipation()); // Participa
            }

            autoSizeColumns(sheet, 4);
            return toBytes(wb);
        }
    }

    // Campañas
    private byte[] exportCampaigns() throws IOException {
        List<ExportCampaignDTO> campaigns = exportService.getCampaigns();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Campañas");
            CellStyle hs = createHeaderStyle(wb);

            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Tipo", "Fecha inicio", "Fecha fin"
            }, hs);

            int r = 1;
            for (ExportCampaignDTO c : campaigns) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(c.getId()); // ID
                row.createCell(1).setCellValue(c.getName()); // Nombre
                row.createCell(2).setCellValue(c.getType()); // Tipo
                row.createCell(3).setCellValue(c.getStartDate()); // Fecha inicio
                row.createCell(4).setCellValue(c.getEndDate()); // Fecha fin
            }

            autoSizeColumns(sheet, 5);
            return toBytes(wb);
        }
    }

    // Entidades colaboradoras 
    private byte[] exportPartners() throws IOException {
        List<ExportPartnerDTO> partners = exportService.getPartners();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Entidades colaboradoras");
            CellStyle hs = createHeaderStyle(wb);

            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Dirección", "Teléfono"
            }, hs);

            int r = 1;
            for (ExportPartnerDTO p : partners) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(p.getId()); // ID
                row.createCell(1).setCellValue(p.getName()); // Nombre
                row.createCell(2).setCellValue(p.getAddress()); // Dirección
                row.createCell(3).setCellValue(p.getPhone()); // Teléfono
            }

            autoSizeColumns(sheet, 4);
            return toBytes(wb);
        }
    }

    // Usuarios 
    private byte[] exportUsers() throws IOException {
        List<ExportUserDTO> users = exportService.getUsers();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Usuarios");
            CellStyle hs = createHeaderStyle(wb);
            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Email", "Teléfono", "Dirección", "CP"
            }, hs);

            int r = 1;
            for (ExportUserDTO u : users) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(u.getId()); // ID
                row.createCell(1).setCellValue(u.getName()); // Nombre
                row.createCell(2).setCellValue(u.getEmail()); // Email
                row.createCell(3).setCellValue(u.getPhone()); // Teléfono
                row.createCell(4).setCellValue(u.getAddress()); // Dirección
                row.createCell(5).setCellValue(u.getPostalCode()); // CP
            }

            autoSizeColumns(sheet, 6);
            return toBytes(wb);
        }
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
            if (sheet.getColumnWidth(i) < 3072) sheet.setColumnWidth(i, 3072);
        }
    }

    private byte[] toBytes(XSSFWorkbook wb) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }
}