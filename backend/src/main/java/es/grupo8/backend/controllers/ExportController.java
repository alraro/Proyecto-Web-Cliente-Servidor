package es.grupo8.backend.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestParam;

import es.grupo8.backend.dao.AdminRepository;
import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.ChainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.entity.AdminEntity;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.ChainEntity;
import es.grupo8.backend.entity.PartnerEntity;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.entity.UserEntity;

@Controller
@RequestMapping("/api/export")
public class ExportController {

    @Autowired 
    private StoreRepository storeRepository;

    @Autowired 
    private ChainRepository chainRepository;

    @Autowired 
    private CampaignRepository campaignRepository;

    @Autowired 
    private PartnerEntityRepository partnerEntityRepository;
    
    @Autowired 
    private UserRepository userRepository;
    
    @Autowired 
    private CampaignStoreRepository campaignStoreRepository;
    
    @Autowired 
    private AdminRepository adminRepository;
    
    @Autowired 
    private CoordinatorRepository coordinatorRepository;
    
    @Autowired 
    private CaptainRepository captainRepository;



    @GetMapping("/{resource}")
    public ResponseEntity<?> export(
            @PathVariable String resource,
            @RequestParam(required = false) Integer campaignId) {
        try {
            byte[] xlsx = switch (resource) {
                case "stores"    -> exportStores();
                case "chains"    -> exportChains();
                case "campaigns" -> exportCampaigns();
                case "partners"  -> exportPartners();
                case "users"     -> exportUsers();
                default          -> null;
            };

            if (xlsx == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Recurso no válido");
            }

            String file = resource + "_export.xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file + "\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(xlsx);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar el archivo: " + e.getMessage());
        }
    }

    // Tiendas
    private byte[] exportStores() throws IOException {
        List<Store> stores = storeRepository.findAll();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Tiendas");
            CellStyle hs = createHeaderStyle(wb);

            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Domicilio", "Localidad", "CP", "Zona", "Cadena"
            }, hs);

            int r = 1;
            for (Store s : stores) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue((double) s.getId()); // ID
                row.createCell(1).setCellValue(s.getName()); // Nombre
                row.createCell(2).setCellValue(nullSafe(s.getAddress())); // Domicilio
                if (s.getPostalCode() != null) {
                    if (s.getPostalCode().getIdLocality() != null) {
                        row.createCell(3).setCellValue(s.getPostalCode().getIdLocality().getName()); // Localidad
                        if (s.getPostalCode().getIdLocality().getIdZone() != null)
                            row.createCell(5).setCellValue(s.getPostalCode().getIdLocality().getIdZone().getName()); // Zona
                    }
                    row.createCell(4).setCellValue(s.getPostalCode().getPostalCode()); // CP
                }
                if (s.getIdChain() != null)
                    row.createCell(6).setCellValue(s.getIdChain().getName()); // Cadena
            }
            autoSizeColumns(sheet, 7);
            return toBytes(wb);
        }
    }

    // Cadenas
    private byte[] exportChains() throws IOException {
        List<ChainEntity> chains = chainRepository.findAll();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Cadenas");
            CellStyle hs = createHeaderStyle(wb);

            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Código", "Participa"
            }, hs);

            int r = 1;
            for (ChainEntity c : chains) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue((double) c.getIdChain()); // ID
                row.createCell(1).setCellValue(c.getName()); // Nombre
                row.createCell(2).setCellValue(c.getCode()); // Código
                row.createCell(3).setCellValue(Boolean.TRUE.equals(c.getParticipation()) ? "Sí" : "No"); // Participa
            }

            autoSizeColumns(sheet, 4);
            return toBytes(wb);
        }
    }

    // Campañas
    private byte[] exportCampaigns() throws IOException {
        List<Campaign> campaigns = campaignRepository.findAll();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Campañas");
            CellStyle hs = createHeaderStyle(wb);

            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Tipo", "Fecha inicio", "Fecha fin"
            }, hs);

            int r = 1;
            for (Campaign c : campaigns) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue((double) c.getId()); // ID
                row.createCell(1).setCellValue(c.getName()); // Nombre
                row.createCell(2).setCellValue(c.getIdType() != null ? c.getIdType().getName() : ""); // Tipo
                row.createCell(3).setCellValue(c.getStartDate().toString()); // Fecha inicio
                row.createCell(4).setCellValue(c.getEndDate().toString()); // Fecha fin
            }

            autoSizeColumns(sheet, 5);
            return toBytes(wb);
        }
    }

    // Entidades colaboradoras 
    private byte[] exportPartners() throws IOException {
        List<PartnerEntity> partners = partnerEntityRepository.findAll();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Entidades colaboradoras");
            CellStyle hs = createHeaderStyle(wb);

            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Dirección", "Teléfono"
            }, hs);

            int r = 1;
            for (PartnerEntity p : partners) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue((double) p.getId()); // ID
                row.createCell(1).setCellValue(p.getName()); // Nombre
                row.createCell(2).setCellValue(nullSafe(p.getAddress())); // Dirección
                row.createCell(3).setCellValue(nullSafe(p.getPhone())); // Teléfono
            }

            autoSizeColumns(sheet, 4);
            return toBytes(wb);
        }
    }

    // Usuarios 
    private byte[] exportUsers() throws IOException {
        List<UserEntity> users = userRepository.findAll();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Usuarios");
            CellStyle hs = createHeaderStyle(wb);
            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Email", "Teléfono", "Dirección", "CP"
            }, hs);

            int r = 1;
            for (UserEntity u : users) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue((double) u.getIdUser()); // ID
                row.createCell(1).setCellValue(u.getName()); // Nombre
                row.createCell(2).setCellValue(u.getEmail()); // Email
                row.createCell(3).setCellValue(nullSafe(u.getPhone())); // Teléfono
                row.createCell(4).setCellValue(nullSafe(u.getAddress())); // Dirección
                row.createCell(5).setCellValue(nullSafe(u.getPostalCode())); // CP
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

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}