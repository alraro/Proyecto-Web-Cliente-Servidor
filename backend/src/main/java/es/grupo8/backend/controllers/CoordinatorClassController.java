package es.grupo8.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class CoordinatorClassController {

	@GetMapping("/coordinator-dashboard")
	public String coordinatorDashboard() {
		return "coordinator-dashboard";
	}

	@GetMapping("/coordinator-campaigns")
	public String coordinatorCampaigns() {
		return "coordinator-campaigns";
	}

	@GetMapping("/coordinator-stores")
	public String coordinatorStores() {
		return "coordinator-stores";
	}

	@GetMapping("/coordinator-captains")
	public String coordinatorCaptains() {
		return "coordinator-captains";
	}

	@GetMapping("/coordinator-volunteers")
	public String coordinatorVolunteers() {
		return "coordinator-volunteers";
	}

	@GetMapping("/coordinator-collaborators")
	public String coordinatorCollaborators() {
		return "coordinator-collaborators";
	}

	@GetMapping("/coordinator-entities")
	public String coordinatorEntities(HttpSession session) {
		return "coordinator-entities";
	}

    /** Redirects to the frontend create-shift page, passing the session JWT via URL so the
	 *  frontend can bootstrap localStorage auth when the user came from the SSR login flow. */
	@GetMapping("/create-shift")
	public String redirectToCreateShift(HttpSession session) {
        return "create-shift";
	}

	/** Redirects to the frontend shifts-calendar page with the same token-bridging mechanism. */
	@GetMapping("/shifts-calendar")
	public String redirectToShiftsCalendar(HttpSession session) {
        return "shifts-calendar";
	}

}
