/**
 * Clase base para los controladores MVC: comprobación de rol y userId de la sesión.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.controllers;

import jakarta.servlet.http.HttpSession;

public abstract class MvcSessionController {

    protected boolean hasRole(HttpSession session, String role) {
        return role.equals(session.getAttribute("role"));
    }

    protected Integer currentUserId(HttpSession session) {
        return (Integer) session.getAttribute("userID");
    }
}
