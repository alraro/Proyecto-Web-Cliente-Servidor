package es.grupo8.backend.services;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class utils {
    // Métodos auxiliares
	// Limpiador de textos, se asegura de no guardar textos vacíos o llenos de espacios
	public static String trimToNull(String value) {
		if (value == null) {
			return null;
		}

		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	// Normaliza el email convirtiéndolo a minúsculas y eliminando espacios innecesarios
	public static String normalizeEmail(String email) {
		String trimmed = trimToNull(email);
		return trimmed == null ? null : trimmed.toLowerCase();
	}

	// Valida el formato del email usando una expresión regular simple
	public static boolean isValidEmail(String email) {
		return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	}

	// Valida el formato del teléfono permitiendo dígitos, espacios, guiones y signos de más, con una longitud razonable.
	public static boolean isValidPhone(String telefono) {
		return telefono != null && telefono.matches("^[0-9+\\-\\s]{7,20}$");
	}
	
	// Valida que el código postal tenga exactamente 5 dígitos y estén entre el 0 y 9, que no sean letras
	public static boolean isValidPostalCode(String cp) {
		return cp != null && cp.matches("^[0-9]{5}$");
	}

	// Métodos relacionados con la gestión de contraseñas usando BCrypt para hashing seguro, verificación de contraseñas y detección de si una contraseña necesita ser migrada a un formato más seguro.
	public static String hashPassword(String rawPassword) {
		return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
	}

	// Verifica si la contraseña proporcionada coincide con la contraseña almacenada, manejando tanto contraseñas sin formato como contraseñas hashadas con BCrypt.
	public static boolean matchesPassword(String rawPassword, String storedPassword) {
		if (rawPassword == null || storedPassword == null) {
			return false;
		}

		if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
			return BCrypt.checkpw(rawPassword, storedPassword);
		}

		return rawPassword.equals(storedPassword);
	}

	// Determina si la contraseña almacenada necesita ser migrada a un formato hashado con BCrypt
	public static boolean needsMigration(String storedPassword) {
		return !(storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$"));
	}
}
