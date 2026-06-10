/**
 * Servicio de exportación de datos.
 *
 * Autores:
 * - Hugo Herrero González: 70%
 * - Fernando Luis Pinilla Molina: 5%
 * - IA Generativa: 25%
 */
/*
*   Participación en el proyecto:
*  - Hugo Herrero González: 70%
*  - IA Generativa: 30%
*/

package es.grupo8.backend.services;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.ChainRepository;
import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dto.ExportCampaignDTO;
import es.grupo8.backend.dto.ExportChainDTO;
import es.grupo8.backend.dto.ExportPartnerDTO;
import es.grupo8.backend.dto.ExportStoreDTO;
import es.grupo8.backend.dto.ExportUserDTO;
import es.grupo8.backend.mapper.ExportCampaignMapper;
import es.grupo8.backend.mapper.ExportChainMapper;
import es.grupo8.backend.mapper.ExportPartnerMapper;
import es.grupo8.backend.mapper.ExportStoreMapper;
import es.grupo8.backend.mapper.ExportUserMapper;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ExportService {

    private final StoreRepository storeRepository;
    private final ChainRepository chainRepository;
    private final CampaignRepository campaignRepository;
    private final PartnerEntityRepository partnerEntityRepository;
    private final UserRepository userRepository;

    private final ExportStoreMapper exportStoreMapper;
    private final ExportChainMapper exportChainMapper;
    private final ExportCampaignMapper exportCampaignMapper;
    private final ExportPartnerMapper exportPartnerMapper;
    private final ExportUserMapper exportUserMapper;


    // Función principal que delega a cada método según el recurso solicitado
    public byte[] generateExcelExport(String resource) throws IOException {
        return switch (resource) {
            case "stores"    -> exportStores();
            case "chains"    -> exportChains();
            case "campaigns" -> exportCampaigns();
            case "partner-entities", "partners" -> exportPartners();
            case "users"     -> exportUsers();
            default          -> null;
        };
    }

    // Tiendas
    private byte[] exportStores() throws IOException {
        // Obtenemos los datos y los convertimos a DTOs
        List<ExportStoreDTO> stores = exportStoreMapper.toDTOList(storeRepository.findAll());

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Tiendas");

            // Creamos la fila de encabezado
            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Domicilio", "Localidad", "CP", "Zona", "Cadena"
            });

            int r = 1;
            for (ExportStoreDTO s : stores) {
                // Creamos una fila por cada tienda y rellenamos las celdas
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(s.getId()); // ID
                row.createCell(1).setCellValue(s.getName()); // Nombre
                row.createCell(2).setCellValue(s.getAddress()); // Domicilio
                row.createCell(3).setCellValue(s.getLocality()); // Localidad
                row.createCell(4).setCellValue(s.getPostalCode()); // CP
                row.createCell(5).setCellValue(s.getZone()); // Zona
                row.createCell(6).setCellValue(s.getChain()); // Cadena
            }

            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i);
            }
            return toBytes(wb);
        }
    }

    // Cadenas
    private byte[] exportChains() throws IOException {
        List<ExportChainDTO> chains = exportChainMapper.toDTOList(chainRepository.findAll());

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Cadenas");

            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Código", "Participa"
            });

            int r = 1;
            for (ExportChainDTO c : chains) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(c.getId()); // ID
                row.createCell(1).setCellValue(c.getName()); // Nombre
                row.createCell(2).setCellValue(c.getCode()); // Código
                row.createCell(3).setCellValue(c.getParticipation()); // Participa
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }
            return toBytes(wb);
        }
    }


    // Campañas
    private byte[] exportCampaigns() throws IOException {
        List<ExportCampaignDTO> campaigns = exportCampaignMapper.toDTOList(campaignRepository.findAll());

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Campañas");

            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Tipo", "Fecha inicio", "Fecha fin"
            });

            int r = 1;
            for (ExportCampaignDTO c : campaigns) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(c.getId()); // ID
                row.createCell(1).setCellValue(c.getName()); // Nombre
                row.createCell(2).setCellValue(c.getType()); // Tipo
                row.createCell(3).setCellValue(c.getStartDate()); // Fecha inicio
                row.createCell(4).setCellValue(c.getEndDate()); // Fecha fin
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            return toBytes(wb);
        }
    }


    // Entidades colaboradoras 
    private byte[] exportPartners() throws IOException {
        List<ExportPartnerDTO> partners = exportPartnerMapper.toDTOList(partnerEntityRepository.findAll());

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Entidades colaboradoras");

            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Dirección", "Teléfono"
            });

            int r = 1;
            for (ExportPartnerDTO p : partners) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(p.getId()); // ID
                row.createCell(1).setCellValue(p.getName()); // Nombre
                row.createCell(2).setCellValue(p.getAddress()); // Dirección
                row.createCell(3).setCellValue(p.getPhone()); // Teléfono
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }
            return toBytes(wb);
        }
    }

    // Usuarios 
    private byte[] exportUsers() throws IOException {
        List<ExportUserDTO> users = exportUserMapper.toDTOList(userRepository.findAll());

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Usuarios");

            createHeaderRow(sheet, new String[]{
                "ID", "Nombre", "Email", "Teléfono", "Dirección", "CP"
            });

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

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }
            return toBytes(wb);
        }
    }


    // Creamos la fila de los encabezados, sino empezaríamos directamente con los datos
    private void createHeaderRow(Sheet sheet, String[] cols) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < cols.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(cols[i]);
        }
    }

    // Convierte el Excel a un array de bytes para enviarlo en la respuesta
    private byte[] toBytes(XSSFWorkbook wb) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }

}