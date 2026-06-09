package es.grupo8.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class CoordinatorClassController {

    /** Serves the create-shift frontend page. */
    @GetMapping("/create-shift")
    public String redirectToCreateShift(HttpSession session) {
        return "create-shift";
    }

    /** Serves the shifts-calendar frontend page. */
    @GetMapping("/shifts-calendar")
    public String redirectToShiftsCalendar(HttpSession session) {
        return "shifts-calendar";
    }

}
