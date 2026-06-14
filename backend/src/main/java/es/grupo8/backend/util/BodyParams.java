/**
 * Utilidades para leer valores de cuerpos JSON recibidos como Map.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 10%
 * - IA Generativa: 90%
 */
package es.grupo8.backend.util;

import java.util.ArrayList;
import java.util.List;

public final class BodyParams {

    private BodyParams() {
    }

    public static Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static List<Integer> toIntegerList(Object value) {
        if (!(value instanceof List<?> rawList)) return null;
        List<Integer> result = new ArrayList<>();
        for (Object item : rawList) {
            Integer parsed = toInteger(item);
            if (parsed == null) return null;
            result.add(parsed);
        }
        return result;
    }

    public static String trimToNull(Object value) {
        if (value == null) return null;
        String s = value.toString().trim();
        return s.isEmpty() ? null : s;
    }
}
