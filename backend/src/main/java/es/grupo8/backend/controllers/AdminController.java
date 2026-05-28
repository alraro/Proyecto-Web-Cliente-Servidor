package es.grupo8.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MVC controller that serves JSP views for the admin section.
 *
 * IMPORTANT — naming convention:
 *   This is @Controller (not @RestController). Methods return view names
 *   that Spring resolves using the prefix/suffix in application.properties:
 *     spring.mvc.view.prefix=/WEB-INF/jsp/
 *     spring.mvc.view.suffix=.jsp
 *
 * Navigation flow:
 *   frontend/admin.html (Nginx :80)
 *       ├── href="http://localhost:8080/admin-coordinators"
 *       │       └── AdminController.adminCoordinators()
 *       │               └── returns "admin-coordinators"
 *       │                       └── WEB-INF/jsp/admin-coordinators.jsp
 *       └── href="http://localhost:8080/admin-captains"
 *               └── AdminController.adminCaptains()
 *                       └── returns "admin-captains"
 *                               └── WEB-INF/jsp/admin-captains.jsp
 *                                       └── "← Volver al menú" → GET /admin
 *                                               └── AdminController.backToMenu()
 *                                                       └── WEB-INF/jsp/admin.jsp
 */
@Controller
public class AdminController {

    // Removed /admin-campaign-assignments because RF-14 now uses dedicated
    // independent views for each role: /admin-coordinators and /admin-captains.

    /**
     * Serves the dedicated coordinator assignment JSP (RF-14).
     *
     * Linked from: frontend/admin.html → "Coordinadores" menu card.
     */
    @GetMapping("/admin-coordinators")
    public String adminCoordinators() {
        return "admin-coordinators";
    }

    /**
     * Serves the dedicated captain assignment JSP (RF-14).
     *
     * Linked from: frontend/admin.html → "Capitanes" menu card.
     */
    @GetMapping("/admin-captains")
    public String adminCaptains() {
        return "admin-captains";
    }

    /**
     * Serves the campaign management JSP (RF-10/RF-11).
     * Linked from: frontend/admin.html → "Campañas" menu card.
     */
    @GetMapping("/admin-campaigns")
    public String adminCampaigns() {
        return "admin-campaigns";
    }

    /**
     * Serves the public campaign listing JSP (RF-13).
     * Accessible to all authenticated roles — not admin-only.
     * Linked from: frontend/admin.html → "Ver campañas" card or direct URL.
     * Maps to: WEB-INF/jsp/campaigns.jsp
     */
    @GetMapping("/campaigns")
    public String campaigns() {
        return "campaigns";
    }

    /**
     * Handles the "Back to menu" link inside admin JSP views.
     *
     * The JSPs live at localhost:8080 but the admin panel lives at localhost:80.
     * A redirect bridges both origins so the user lands back on the correct page.
     *
     * Used by: admin-campaign-assignments.jsp → <a href="/admin">← Back to menu</a>
     */
    @GetMapping("/admin")
    public String backToMenu() {
        return "admin";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/admin-chains")
    public String adminChains() { return "admin-chains"; }

    @GetMapping("/admin-stores")
    public String adminStores() { return "admin-stores"; }

    @GetMapping("/admin-validate-users")
    public String adminValidateUsers() { return "admin-validate-users"; }

    @GetMapping("/responsible-store")
    public String responsibleStore() { return "responsible-store"; }
}