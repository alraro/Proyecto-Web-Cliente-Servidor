# Prompt — Backend Refactor: AuthException pattern + REST/MVC split + JSP JSTL + Tests

## Project context

Spring Boot monolith. Package root: `es.grupo8.backend`. Java 17. Maven.

Directory layout:
```
backend/src/main/java/es/grupo8/backend/
  controllers/          ← @Controller (MVC/JSP) and legacy @RestControllers
  controllers/rest/     ← proper @RestControllers already moved here
  services/
  dao/
  dto/
  entity/
  security/             ← AdminGuard, CoordinatorGuard, CaptainGuard
  exceptions/           ← AuthException.java ALREADY EXISTS
  mapper/
backend/src/main/webapp/WEB-INF/jsp/   ← all JSP views
```

Guards already implemented:
- `AdminGuard.isAdmin(authHeader)` / `AdminGuard.extractUserId(authHeader)`
- `CoordinatorGuard.isCoordinator(authHeader)` / `CoordinatorGuard.extractUserId(authHeader)`
- `CaptainGuard.isUserCaptain(authHeader)` / `CaptainGuard.extractUserId(authHeader)`

`AuthException.java` already exists:
```java
package es.grupo8.backend.exceptions;
import org.springframework.http.HttpStatus;
public class AuthException extends RuntimeException {
    private final HttpStatus status;
    public AuthException(HttpStatus status, String message) { super(message); this.status = status; }
    public HttpStatus getStatus() { return status; }
}
```

Session attributes set on login (in `ViewsController.submitLogin`):
- `session.getAttribute("token")` → JWT string (WITHOUT "Bearer " prefix)
- `session.getAttribute("userID")` → Integer
- `session.getAttribute("role")` → String

## Scope — ONLY touch files that belong to the user

The user's modules are: **campaigns, coordinators, captains, admin-captain-requests**.

**DO NOT touch** any files related to: partner-entities, volunteers (standalone), stores, chains, users, auth, dashboard, export, responsible-store, collaborator. Those belong to teammates.

---

## Task 1 — Create GlobalExceptionHandler (new file)

Create `es.grupo8.backend.exceptions.GlobalExceptionHandler`:

```java
package es.grupo8.backend.exceptions;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST controllers.
 * Catches AuthException thrown by checkAuth() methods and returns the appropriate HTTP status.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(AuthException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
    }
}
```

---

## Task 2 — Move user's @RestControllers to `controllers/rest/`

Move the following files from `controllers/` to `controllers/rest/` and rename them (add "Rest" suffix to class name and filename). Update `@RequestMapping` URLs if needed (they must stay the same). Apply the `checkAuth` + `AuthException` pattern replacing every `if (!guard.isX()) return forbidden()` block.

### Files to move and refactor:

#### 2.1 `CampaignController.java` → `controllers/rest/CampaignRestController.java`

- Class rename: `CampaignController` → `CampaignRestController`
- Keep `@RequestMapping("/api")`
- Remove the local `@ExceptionHandler` blocks for `IllegalArgumentException`, `NoSuchElementException`, `IllegalStateException` — these are now handled globally. Keep them ONLY if the GlobalExceptionHandler doesn't cover them (GlobalExceptionHandler only covers AuthException, so keep the others).
- Replace auth checks: the current pattern is `if (!adminGuard.isAdmin(authHeader)) return ResponseEntity.status(FORBIDDEN)...`. Replace with a private `checkAdmin(String auth)` method:
  ```java
  private void checkAdmin(String auth) {
      Integer userId = adminGuard.extractUserId(auth);
      if (userId == null) throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
      if (!adminGuard.isAdmin(auth)) throw new AuthException(HttpStatus.FORBIDDEN, "Access restricted to administrators");
  }
  ```
  Then each endpoint calls `checkAdmin(authHeader)` as first line and returns its clean type.
- Remove local `@ExceptionHandler(AuthException.class)` since it's global now.

#### 2.2 `CampaignAssignmentController.java` → `controllers/rest/CampaignAssignmentRestController.java`

- Class rename: `CampaignAssignmentController` → `CampaignAssignmentRestController`
- Same pattern: replace all `if (!adminGuard.isAdmin(authHeader)) return forbidden()` with `checkAdmin(authHeader)` using `AuthException`.
- Keep local handlers for `IllegalArgumentException`, `NoSuchElementException`, `IllegalStateException`.

#### 2.3 `CampaignStoreController.java` → `controllers/rest/CampaignStoreRestController.java`

- Class rename: `CampaignStoreController` → `CampaignStoreRestController`
- Same pattern with `checkAdmin`.

#### 2.4 `CoordinatorController.java` → `controllers/rest/CoordinatorRestController.java`

- Class rename: `CoordinatorController` → `CoordinatorRestController`
- `@RequestMapping("/api/shifts")` stays.
- Replace all `if (!coordinatorGuard.isCoordinator(authHeader))` checks with `checkCoordinator(authHeader)`:
  ```java
  private void checkCoordinator(String auth) {
      Integer userId = coordinatorGuard.extractUserId(auth);
      if (userId == null) throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
      if (!coordinatorGuard.isCoordinator(auth)) throw new AuthException(HttpStatus.FORBIDDEN, "Acceso denegado. Solo los coordinadores pueden realizar esta acción.");
  }
  ```
- Remove the `@ExceptionHandler(RuntimeException.class)` (too broad). Keep `IllegalArgumentException` handler.
- Remove the `auditLog.warn` inside the auth check (it'll now be in the guard layer).

#### 2.5 `CoordinatorDashboardController.java` → `controllers/rest/CoordinatorDashboardRestController.java`

- Class rename: `CoordinatorDashboardController` → `CoordinatorDashboardRestController`
- Same pattern with `checkCoordinator`.

#### 2.6 `CaptainDashboardController.java` → `controllers/rest/CaptainDashboardRestController.java`

- Class rename: `CaptainDashboardController` → `CaptainDashboardRestController`
- Replace all `if (!captainGuard.isUserCaptain(authHeader)) return forbidden()` with `checkCaptain(authHeader)`:
  ```java
  private void checkCaptain(String auth) {
      Integer userId = captainGuard.extractUserId(auth);
      if (userId == null) throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
      if (!captainGuard.isUserCaptain(auth)) throw new AuthException(HttpStatus.FORBIDDEN, "Acceso denegado");
  }
  ```

#### 2.7 `CaptainShiftController.java` → `controllers/rest/CaptainShiftRestController.java`

- Class rename: `CaptainShiftController` → `CaptainShiftRestController`
- Same `checkCaptain` pattern.
- NOTE: `@RequestMapping("/api/shifts")` conflicts with `CoordinatorRestController`. The endpoint `GET /api/shifts/my-team` is exclusive to captains, so keep the mapping — Spring differentiates by the sub-path `/my-team`.

#### 2.8 `AdminCaptainRequestController.java` → `controllers/rest/AdminCaptainRequestRestController.java`

- Class rename: `AdminCaptainRequestController` → `AdminCaptainRequestRestController`
- Switch from `@Autowired` field injection to constructor injection (`@AllArgsConstructor` + `final` fields) for consistency with rest of codebase.
- Replace `if (!adminGuard.isAdmin(authHeader)) return forbidden()` with `checkAdmin(authHeader)`.
- Move `requestToMap` helper to a proper private method (keep as-is, just clean up).

---

## Task 3 — Create MVC @Controller classes in `controllers/` for JSP views

These controllers serve JSPs by loading data from services and injecting via `Model`. They use `HttpSession` to get the JWT token and pass `"Bearer " + token` to the guards.

Auth pattern for MVC controllers (redirect on failure instead of throwing):
```java
private String bearerToken(HttpSession session) {
    Object token = session.getAttribute("token");
    return token != null ? "Bearer " + token : null;
}
```
If the guard returns false → `return "redirect:/login"`.

### 3.1 Create `controllers/CampaignViewController.java`

```
Package: es.grupo8.backend.controllers
Annotation: @Controller
Dependencies (constructor injection): AdminGuard, CampaignService, CampaignAssignmentService
```

Routes to implement (remove these from `ViewsController.java` after adding them here):

| Method | Path | JSP view | Data loaded into Model |
|--------|------|----------|----------------------|
| GET | `/admin-campaigns` | `admin-campaigns` | `campaigns` (Page, page=0,size=10), `campaignTypes`, `counts` (array from getCampaignCounts()) |
| GET | `/admin-campaign-assignments` | `admin-campaign-assignments` | `campaigns` (list from CampaignAssignmentService.getCampaigns) |
| GET | `/admin-captains` | `admin-captains` | `campaigns` (list from CampaignAssignmentService.getCampaigns) |
| GET | `/admin-coordinators` | `admin-coordinators` | `campaigns` (list from CampaignAssignmentService.getCampaigns) |

Each method:
1. Gets token from session, builds bearer string
2. Checks admin guard — if not admin, `return "redirect:/login"`
3. Calls service to load data
4. Adds data to Model
5. Returns view name (string)

### 3.2 Create `controllers/CoordinatorViewController.java`

```
Package: es.grupo8.backend.controllers
Annotation: @Controller
Dependencies: CoordinatorGuard, CoordinatorDashboardService, ShiftService
```

Routes (remove from `ViewsController.java`):

| Method | Path | JSP view | Data loaded into Model |
|--------|------|----------|----------------------|
| GET | `/coordinator` | `coordinator` | `userName` from session |
| GET | `/coordinator-dashboard` | `coordinator-dashboard` | `userName`, `myCampaigns` (getMyCampaigns) |
| GET | `/coordinator-campaigns` | `coordinator-campaigns` | `campaigns` (getMyCampaigns) |
| GET | `/coordinator-stores` | `coordinator-stores` | `campaigns` (getMyCampaigns) — storeId loaded dynamically via JS/API |
| GET | `/coordinator-captains` | `coordinator-captains` | `campaigns` (getMyCampaigns), `captains` (getCaptains with null campaignId) |
| GET | `/coordinator-volunteers` | `coordinator-volunteers` | `volunteers` (getVolunteers), `partnerEntities` (getPartnerEntities) |
| GET | `/coordinator-collaborators` | `coordinator-collaborators` | `campaigns` (getMyCampaigns) |
| GET | `/coordinator-entities` | `coordinator-entities` | `partnerEntities` (getPartnerEntities) |
| GET | `/create-shift` | `create-shift` (if exists) | `campaigns` (getMyCampaigns) |

### 3.3 Create `controllers/CaptainViewController.java`

```
Package: es.grupo8.backend.controllers
Annotation: @Controller
Dependencies: CaptainGuard, CaptainDashboardService
```

Routes (remove from `ViewsController.java`):

| Method | Path | JSP view | Data loaded into Model |
|--------|------|----------|----------------------|
| GET | `/captain` | `captain` | `userName` from session |
| GET | `/captain-dashboard` | `captain-dashboard` | `userName`, `campaigns` (getMyCampaigns) |
| GET | `/captain-stores` | `captain-stores` | `campaigns` (getMyCampaigns) |
| GET | `/captain-incidents` | `captain-incidents` | `campaigns` (getMyCampaigns) |
| GET | `/captain-attendance` | `captain-attendance` | `campaigns` (getMyCampaigns) |

### 3.4 Create `controllers/AdminCaptainRequestViewController.java`

```
Package: es.grupo8.backend.controllers
Annotation: @Controller
Dependencies: AdminGuard, CaptainRequestRepository (read-only list)
```

Routes (remove from `ViewsController.java` if present):

| Method | Path | JSP view | Data loaded into Model |
|--------|------|----------|----------------------|
| GET | `/admin-captain-requests` | `admin-captain-requests` | `pendingRequests` (captainRequestRepository.findByStatus("PENDIENTE")) |

---

## Task 4 — Remove migrated routes from `ViewsController.java`

Remove only these `@GetMapping` methods from `ViewsController.java` (they are now handled by the new MVC controllers above):
- `coordinator()`, `coordinatorDashboard()`, `coordinatorCampaigns()`, `coordinatorStores()`, `coordinatorCaptains()`, `coordinatorVolunteers()`, `coordinatorCollaborators()`, `coordinatorEntities()`
- `captain()`, `captainDashboard()`, `captainStores()`, `captainIncidents()`, `captainAttendance()`

Do NOT remove or touch: `doInit`, `doLogin`, `submitLogin`, `doRegister`, `submitRegister`, `logout`, `editProfile`, `submitEditProfile`, `cancelEdit`, `collaborator`, `resolveRolePath`. Those are NOT the user's.

Also do NOT add any routing for admin pages that ViewsController doesn't already handle — those were already not there or are handled by other controllers.

---

## Task 5 — Refactor JSPs to use JSTL

For each JSP listed below, refactor so that **initial data rendering uses JSTL** (`<c:forEach>`, `<c:if>`, `${...}` EL expressions) rather than JavaScript populating empty tbodies. JavaScript should only remain for **dynamic interactions** (form submissions, AJAX filtering, live updates).

Add at the top of each JSP:
```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
```

### 5.1 `coordinator-campaigns.jsp`
Current: empty `<tbody id="campaigns-tbody">` filled by `coordinator-campaigns.js` via fetch.
Refactor: iterate `${campaigns}` with `<c:forEach var="c" items="${campaigns}">` to render rows. Keep JS only for any interactive features (filter, pagination calls). The static table of assigned campaigns renders server-side.

### 5.2 `coordinator-captains.jsp`
Render `${captains}` list server-side. Campaigns selector populated from `${campaigns}`.

### 5.3 `coordinator-volunteers.jsp`
Render `${volunteers}` list server-side. Partner entities selector from `${partnerEntities}`.

### 5.4 `coordinator-entities.jsp`
Render `${partnerEntities}` list server-side.

### 5.5 `captain-dashboard.jsp`
Add welcome with `${userName}`. Campaign list `${campaigns}` rendered server-side if shown.

### 5.6 `captain-stores.jsp`
Render initial campaign selector from `${campaigns}`. Store list loaded dynamically via JS (acceptable, as it depends on campaign selection).

### 5.7 `captain-incidents.jsp`
Campaign selector from `${campaigns}` rendered server-side.

### 5.8 `captain-attendance.jsp`
Campaign selector from `${campaigns}` rendered server-side.

### 5.9 `admin-campaigns.jsp`
Render initial campaign list from `${campaigns.content}` via JSTL. Campaign type filter from `${campaignTypes}`. Keep JS for pagination and status filtering (AJAX).

### 5.10 `admin-campaign-assignments.jsp`
Campaign selector from `${campaigns}` rendered server-side.

### 5.11 `admin-captains.jsp`
Campaign selector from `${campaigns}` rendered server-side.

### 5.12 `admin-coordinators.jsp`
Campaign selector from `${campaigns}` rendered server-side.

### 5.13 `admin-captain-requests.jsp`
Render `${pendingRequests}` list server-side using JSTL. Keep JS only for the approve/reject AJAX actions (POST endpoints remain unchanged).

**JSTL pattern to follow:**
```jsp
<c:choose>
    <c:when test="${empty campaigns}">
        <tr><td colspan="4" class="table-empty">No hay campañas disponibles.</td></tr>
    </c:when>
    <c:otherwise>
        <c:forEach var="campaign" items="${campaigns}">
            <tr>
                <td>${campaign['name']}</td>
                <td>${campaign['type']}</td>
                <td>${campaign['startDate']}</td>
                <td>${campaign['endDate']}</td>
            </tr>
        </c:forEach>
    </c:otherwise>
</c:choose>
```
Note: services return `List<Map<String, Object>>` so use `${item['key']}` syntax. For `CampaignDTO` objects use `${item.name}` bean notation.

---

## Task 6 — Create Unit Tests

Create test directory structure:
```
backend/src/test/java/es/grupo8/backend/
  controllers/rest/
  services/
  controllers/
```

### 6.1 `CampaignRestControllerTest.java`

Test class for `CampaignRestController`. Use `@WebMvcTest(CampaignRestController.class)` with mocked services and guards.

Tests to write:
- `getCampaigns_returnsOk()` — mock service, verify 200
- `createCampaign_notAdmin_returnsForbidden()` — mock `adminGuard.isAdmin` returns false → expect 403
- `createCampaign_noToken_returnsUnauthorized()` — no auth header → expect 401
- `createCampaign_admin_returnsCreated()` — mock guard + service → expect 201
- `deleteCampaign_notFound_returns404()` — service throws `NoSuchElementException` → expect 404

### 6.2 `CoordinatorDashboardRestControllerTest.java`

Tests:
- `getMyCampaigns_notCoordinator_returnsForbidden()` — 403
- `getMyCampaigns_validToken_returnsOk()` — 200 with list
- `createVolunteer_missingBody_returns400()` — null body → 400
- `assignVolunteerShift_notCoordinator_returnsForbidden()` — 403

### 6.3 `CaptainDashboardRestControllerTest.java`

Tests:
- `getMyCampaigns_notCaptain_returnsForbidden()` — 403
- `getShifts_validCaptain_returnsOk()` — 200
- `createIncident_missingBody_returns400()` — 400
- `createIncident_validRequest_returns201()` — 201

### 6.4 `CampaignServiceTest.java`

Unit tests for `CampaignService` with mocked repositories:
- `getCampaignById_exists_returnsDto()` — mock repo, verify mapper called
- `getCampaignById_notExists_returnsEmpty()` — empty Optional
- `createCampaign_duplicateName_throwsIllegalStateException()` — mock `existsByName` = true → `IllegalStateException`
- `deleteCampaign_notFound_throwsNoSuchElementException()` — mock repo returns empty → `NoSuchElementException`

### 6.5 `CoordinatorViewControllerTest.java`

Tests for MVC controller (use `@WebMvcTest(CoordinatorViewController.class)`):
- `coordinatorCampaigns_notCoordinator_redirectsToLogin()` — guard returns false → redirect
- `coordinatorCampaigns_validSession_returnsViewWithModel()` — guard ok, service returns list → view name "coordinator-campaigns", model has "campaigns"

---

## Implementation rules

1. **Do not modify** any file outside the listed scope. Specifically never touch: `PartnerEntityController`, `PartunteerController`, `VolunteerController`, `PartnerEntityManagerController`, `StoreController`, `ChainController`, `UserController`, `AdminController`, `AuthController`, `DashboardController`, `ExportController`, `ResponsibleStoreController`, `ResponsibleCollaboratingController`, `LocalityZoneController`, `ShiftAssignmentController`, and their corresponding services/repos/DTOs/mappers.

2. **Package declarations** must be updated in moved files: `controllers/rest/` → `package es.grupo8.backend.controllers.rest;`

3. **All code, comments, Javadoc in English** (per project conventions).

4. **Constructor injection** (`@AllArgsConstructor` + `final`) for all new and refactored classes, replacing `@Autowired` field injection where possible.

5. **Logging**: Keep existing `auditLog` calls in `AdminCaptainRequestRestController`. Remove the `auditLog.warn` inside the coordinator auth check (it was inside the `if` block that's now replaced).

6. **ViewsController**: Only remove the methods listed in Task 4. Do not refactor or add anything else to this file.

7. **`pom.xml`**: Do not modify dependencies. `spring-boot-starter-test` is already present.

8. **JSTL**: Before using JSTL in JSPs, verify that `jakarta.servlet.jsp.jstl` or `javax.servlet.jsp.jstl` is in `pom.xml`. If not present, add:
   ```xml
   <dependency>
       <groupId>jakarta.servlet.jsp.jstl</groupId>
       <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
   </dependency>
   <dependency>
       <groupId>org.glassfish.web</groupId>
       <artifactId>jakarta.servlet.jsp.jstl</artifactId>
   </dependency>
   ```

9. **Do not break existing JavaScript** in the JSP files. JS files (`coordinator-campaigns.js`, etc.) may still be used for dynamic interactions. Only replace the initial static rendering from JS → JSTL. If a JS file does nothing but fetch+render static data that is now server-side rendered, remove that specific fetch block from the JS file but keep the rest.

10. **After all changes**, verify the project compiles by checking for obvious import errors (class renames, package moves). Run a mental compilation pass over each file.
