package es.grupo8.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CaptainClassController {

	@GetMapping("/captain-dashboard")
	public String captainDashboard() {
		return "captain-dashboard";
	}

	@GetMapping("/captain-stores")
	public String captainStores() {
		return "captain-stores";
	}

	@GetMapping("/captain-incidents")
	public String captainIncidents() {
		return "captain-incidents";
	}

	@GetMapping("/captain-attendance")
	public String captainAttendance() {
		return "captain-attendance";
	}
}
