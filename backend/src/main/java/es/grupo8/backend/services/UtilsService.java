/**
 *
 * Autores:
 * - Hugo Herrero González: 90%
 * - IA Generativa: 10%
 */

package es.grupo8.backend.services;

<<<<<<< HEAD
=======
import es.grupo8.backend.dto.PaginatedResponse;
import java.util.List;
>>>>>>> 4ba6d542aef4e5958b43aa0e51f616e2074360cc
import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCrypt;

public final class UtilsService {

    public static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9()\\-\\s]{7,20}$");

    private UtilsService() {
    }

    public record SortInfo(String field, String order) {}

    public static SortInfo parseSort(String sort) {
        String field = "id";
        String order = "asc";
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            field = parts[0].trim().toLowerCase();
            order = parts.length > 1 && "desc".equals(parts[1].trim().toLowerCase()) ? "desc" : "asc";
        }
        return new SortInfo(field, order);
    }

    public static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static boolean isValidPhone(String telefono) {
        return telefono != null && telefono.matches("^[0-9+\\-\\s]{7,20}$");
    }

    public static boolean isValidPostalCode(String cp) {
        return cp != null && cp.matches("^[0-9]{5}$");
    }

    public static String normalizePhone(String phone) {
        String trimmed = trimToNull(phone);
        if (trimmed == null) return null;
        return trimmed.replaceAll("\\s+", " ");
    }

    public static int clampPageSize(int size) {
        return clampPageSize(size, 100);
    }

    public static int clampPageSize(int size, int max) {
        return Math.max(1, Math.min(size, max));
    }

    public static <T> PaginatedResponse<T> buildPaginatedResponse(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PaginatedResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page < totalPages - 1,
                page > 0
        );
    }
}
