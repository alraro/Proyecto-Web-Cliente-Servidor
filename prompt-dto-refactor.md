# Prompt — DTO Refactor: eliminar Map<String, Object> de servicios

## Contexto del problema

Tres servicios devuelven `Map<String, Object>` en lugar de DTOs tipados, violando
las convenciones del proyecto (CLAUDE.md §6 "Obligatorio el uso de DTOs").
El problema está en:

- `services/CaptainDashboardService.java`
- `services/CoordinatorDashboardService.java`
- `services/CampaignAssignmentService.java`

**Scope estricto:** solo estos tres servicios y sus controladores REST directos
(`controllers/rest/CaptainDashboardRestController`, `CoordinatorDashboardRestController`,
`CampaignAssignmentRestController`). No tocar nada más.

---

## Inventario de Maps a eliminar

### CaptainDashboardService

| Método privado / inline | Claves del Map | DTO a crear |
|---|---|---|
| `campaignToMap(Campaign)` | id, name, startDate, endDate, typeName | `CampaignSummaryDto` |
| `storeToMap(CampaignStore)` | id, name, address, chainName, locality | `StoreSummaryDto` |
| `shiftToMap(Shift)` | shiftId, campaignId, storeId, day, startTime, endTime, volunteersNeeded, observations | `ShiftSummaryDto` |
| `volunteerShiftToMap(VolunteerShift)` | volunteerId, volunteerName, phone, shiftDay, startTime, endTime, attendance | `VolunteerShiftSummaryDto` |
| inline en `getIncidents` | id, description, createdAt, campaignName, storeName | `IncidentSummaryDto` |

### CoordinatorDashboardService

| Método privado / inline | Claves del Map | DTO a usar/crear |
|---|---|---|
| `campaignToMap(Campaign)` | id, name, startDate, endDate, typeName | **Reusar** `CampaignSummaryDto` (misma estructura) |
| `storeToMap(CampaignStore)` | id, name, address, chainName, locality | **Reusar** `StoreSummaryDto` (misma estructura) |
| `volunteerToMap(Volunteer)` | id, name, phone, email, address, partnerEntityId, partnerEntityName | **Reusar** `VoluntarioResponseDto` existente — los campos coinciden, dejar `campaigns` como null |
| inline en `getCaptains` | userId, name, email | `UserSummaryDto` |
| inline en `getPartnerEntities` | id, name, phone | `PartnerEntitySummaryDto` |
| inline en `getCampaignEntities` | id, name, phone, volunteerCount | `CampaignEntityDto` |

### CampaignAssignmentService

| Método privado / inline | Claves del Map | DTO a usar/crear |
|---|---|---|
| `toCampaignMap(Campaign)` | id, name, startDate, endDate, type | **Reusar** `CampaignSummaryDto` (campo `type` → `typeName`) |
| `toUserMap(UserEntity)` | userId, name, email | **Reusar** `UserSummaryDto` |
| `getCampaignAssignments` retorna `Map` con coordinators + captains | campaignId, campaignName, coordinators, captains | `CampaignAssignmentsDto` |
| `assignCoordinator` / `assignCaptain` retorna `Map` | message, campaignId, userId, userName | `AssignmentResultDto` |

---

## DTOs a crear en `dto/`

Todos en `package es.grupo8.backend.dto`. Usar `@Data` de Lombok.
Documentar cada campo con Javadoc.

### 1. `CampaignSummaryDto.java`
```java
@Data
public class CampaignSummaryDto {
    private Integer id;
    private String name;
    private String startDate;   // ISO string "YYYY-MM-DD"
    private String endDate;
    private String typeName;
}
```

### 2. `StoreSummaryDto.java`
```java
@Data
public class StoreSummaryDto {
    private Integer id;
    private String name;
    private String address;
    private String chainName;
    private String locality;
}
```

### 3. `ShiftSummaryDto.java`
```java
@Data
public class ShiftSummaryDto {
    private Integer shiftId;
    private Integer campaignId;
    private Integer storeId;
    private String day;          // ISO string
    private String startTime;    // "HH:mm:ss"
    private String endTime;
    private Integer volunteersNeeded;
    private String observations;
}
```

### 4. `VolunteerShiftSummaryDto.java`
```java
@Data
public class VolunteerShiftSummaryDto {
    private Integer volunteerId;
    private String volunteerName;
    private String phone;
    private String shiftDay;
    private String startTime;
    private String endTime;
    private Boolean attendance;
}
```

### 5. `IncidentSummaryDto.java`
```java
@Data
public class IncidentSummaryDto {
    private Integer id;
    private String description;
    private String createdAt;    // Instant.toString()
    private String campaignName;
    private String storeName;
}
```

### 6. `UserSummaryDto.java`
```java
@Data
public class UserSummaryDto {
    private Integer userId;
    private String name;
    private String email;
}
```

### 7. `PartnerEntitySummaryDto.java`
```java
@Data
public class PartnerEntitySummaryDto {
    private Integer id;
    private String name;
    private String phone;
}
```

### 8. `CampaignEntityDto.java`
```java
@Data
public class CampaignEntityDto {
    private Integer id;
    private String name;
    private String phone;
    private Long volunteerCount;
}
```

### 9. `CampaignAssignmentsDto.java`
```java
@Data
public class CampaignAssignmentsDto {
    private Integer campaignId;
    private String campaignName;
    private List<UserSummaryDto> coordinators;
    private List<UserSummaryDto> captains;
}
```

### 10. `AssignmentResultDto.java`
```java
@Data
public class AssignmentResultDto {
    private String message;
    private Integer campaignId;
    private Integer userId;
    private String userName;
}
```

---

## Cambios en los servicios

### Regla general
- Eliminar todos los métodos privados `xToMap()` y los inline `Map.of(...)` / `new HashMap<>()`.
- Reemplazarlos por métodos privados `toXDto()` que devuelven el DTO correspondiente.
- Cambiar las firmas públicas de `List<Map<String, Object>>` / `Map<String, Object>`
  a `List<NombreDto>` / `NombreDto`.

### CaptainDashboardService — cambios de firma

```java
// ANTES                                        → DESPUÉS
List<Map<String,Object>> getMyCampaigns(...)    → List<CampaignSummaryDto>
List<Map<String,Object>> getMyStores(...)       → List<StoreSummaryDto>
List<Map<String,Object>> getShifts(...)         → List<ShiftSummaryDto>
List<Map<String,Object>> getVolunteerShifts(...)→ List<VolunteerShiftSummaryDto>
// createIncident devuelve Integer — NO CAMBIA
List<Map<String,Object>> getIncidents(...)      → List<IncidentSummaryDto>
```

Eliminar métodos privados: `campaignToMap`, `storeToMap`, `shiftToMap`, `volunteerShiftToMap`.
Añadir privados: `toCampaignSummary`, `toStoreSummary`, `toShiftSummary`, `toVolunteerShiftSummary`, `toIncidentSummary`.

### CoordinatorDashboardService — cambios de firma

```java
List<Map<String,Object>> getMyCampaigns(...)      → List<CampaignSummaryDto>
List<Map<String,Object>> getMyStores(...)         → List<StoreSummaryDto>
List<Map<String,Object>> getVolunteers()          → List<VoluntarioResponseDto>
Map<String,Object>       createVolunteer(...)     → VoluntarioResponseDto
Map<String,Object>       updateVolunteer(...)     → VoluntarioResponseDto
// assignVolunteerShift devuelve void — NO CAMBIA
List<Map<String,Object>> getCaptains(...)         → List<UserSummaryDto>
Map<String,Object>       registerCaptain(...)     → Map<String,Object>  ← MANTENER como Map:
                                                    solo tiene "message" y "requestId",
                                                    no justifica un DTO propio
List<Map<String,Object>> getPartnerEntities()     → List<PartnerEntitySummaryDto>
List<Map<String,Object>> getCampaignEntities(...) → List<CampaignEntityDto>
```

Para `getVolunteers`, `createVolunteer` y `updateVolunteer`: reusar `VoluntarioResponseDto`
existente. El campo `campaigns` quedará null (no se carga en este contexto, es aceptable).

Eliminar privados: `campaignToMap`, `storeToMap`, `volunteerToMap`.
Añadir privados: `toCampaignSummary`, `toStoreSummary` — o extraer la conversión inline.

### CampaignAssignmentService — cambios de firma

```java
List<Map<String,Object>> getCampaigns(...)           → List<CampaignSummaryDto>
Map<String,Object>       getCampaignAssignments(...) → CampaignAssignmentsDto
List<Map<String,Object>> getAvailableUsers(...)      → List<UserSummaryDto>
Map<String,Object>       assignCoordinator(...)      → AssignmentResultDto
Map<String,Object>       assignCaptain(...)          → AssignmentResultDto
// unassignCoordinator / unassignCaptain devuelven void — NO CAMBIAN
```

Eliminar privados: `toCampaignMap`, `toUserMap`, `usersFromCoordinators`, `usersFromCaptains`,
`getAvailableCoordinatorUsers`, `getAvailableCaptainUsers`.

Reescribir esos helpers usando los nuevos DTOs:
```java
private CampaignSummaryDto toCampaignSummary(Campaign c) { ... }
private UserSummaryDto toUserSummary(UserEntity u) { ... }
private List<UserSummaryDto> usersFromCoordinators(List<Coordinator> list) { ... }
private List<UserSummaryDto> usersFromCaptains(List<Captain> list) { ... }
private List<UserSummaryDto> getAvailableCoordinators(Integer campaignId) { ... }
private List<UserSummaryDto> getAvailableCaptains(Integer campaignId) { ... }
```

---

## Cambios en los controladores REST

Los tres controladores `CaptainDashboardRestController`,
`CoordinatorDashboardRestController` y `CampaignAssignmentRestController`
usan `ResponseEntity<?>` en todos sus endpoints — no es necesario cambiar
las anotaciones. Sin embargo, actualizar los comentarios Javadoc de retorno
para reflejar el DTO real que ahora devuelve cada endpoint.

No es necesario cambiar ninguna URL ni lógica de controller.

---

## Reglas de implementación

1. **No tocar** ningún servicio fuera de los tres listados.
2. **No tocar** `VoluntarioResponseDto` ni ningún DTO existente — solo crear nuevos.
3. Todos los nuevos DTOs en **inglés**, con Javadoc en cada campo.
4. Los nuevos métodos `toXDto()` privados son **métodos de servicio**, no mappers
   separados — no crear clases Mapper adicionales para este refactor.
5. Si un campo de entidad puede ser null (fecha, cadena, relación JPA), usar
   `campo != null ? campo.toString() : null` — no lanzar NullPointerException.
6. Después de los cambios, `mvn compile -pl backend` debe compilar sin errores.
7. **No actualizar los JSPs** en este refactor. Los JSPs que usan `${item['key']}`
   seguirán funcionando si los MVC controllers inyectan los DTOs en el Model,
   ya que Spring EL acepta tanto notación de propiedades (`${dto.name}`) como
   de índice (`${dto['name']}`). Dejarlo para un refactor posterior de JSPs.

---

## Verificación

Tras implementar, confirmar:
- `mvn compile -pl backend` sin errores.
- Grep que no quede ningún `Map<String, Object>` como tipo de retorno de método
  público en los tres servicios:
  ```
  grep -n "public.*Map<String, Object>" services/CaptainDashboardService.java
  grep -n "public.*Map<String, Object>" services/CoordinatorDashboardService.java
  grep -n "public.*Map<String, Object>" services/CampaignAssignmentService.java
  ```
  Solo `registerCaptain` puede quedar como `Map` (explicado arriba).
- Los 10 nuevos DTOs existen en `dto/`.
