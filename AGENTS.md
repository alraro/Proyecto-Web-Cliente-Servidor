# Proyecto Web Cliente Servidor - Bancosol | Documentación y Reglas de Arquitectura

## 1. Contexto y Finalidad del Proyecto
- **Descripción:** Este proyecto es una aplicación full stack diseñada para la empresa de banco de alimentos Bancosol. El fin de esta es gestionar roles, con diferentes vistas (admin, coordinador, capitan, responsable de entidad colaboradora etc...) y gestionar campañas, turnos de voluntariado, informacion y modificacion de los usuarios, etc...
- **Objetivo principal:** Pasar de depender a un excel con muchas paginas a una aplicación web que permita gestionar toda la información de manera centralizada, eficiente y segura.
- **Público objetivo / Usuarios:** En su mayoria personal de bancosol o empresas colaboradoras de las campañas de bancosol, como los capitanes o coordinadores.

## 2. Requisitos Base y Repositorio
- **Repositorio Principal:** https://github.com/alraro/Proyecto-Web-Cliente-Servidor
- **Stack Tecnológico Core:**
  - Backend: SpringBoot 4.0.5, java 17, maven 4.0.0
  - Frontend: html5, css, javascript, y en las ultimas etapas del proyecto se añadira React.
  - Base de datos: PostgreSQL 15 (alpine)
- **Gestor de paquetes:** Maven para el backend

## 3. Estructura del Backend
- **Arquitectura:** Monolítica tradicional con separación clara de capas (Controllers, Services, Repositories).
- **Estructura de directorios base:**
  - `/src/main/java/es/grupo8/backend/controllers`: Los controladores que manejan las rutas y endpoints de la API y los endpoints SSR si hay.
  - `/src/main/java/es/grupo8/backend/services`: Servicios que contienen la lógica de negocio y orquestan las operaciones entre los controladores y los repositorios.
  - `/src/main/java/es/grupo8/backend/config`: Configuraciones generales del proyecto (CORS, OpenAPI, etc.)
  - `/src/main/java/es/grupo8/backend/dao`: Repositorios de acceso a datos, si se utilizan para mapear consultas personalizadas o vistas, el nombre de los archivos sigue el patron [Nombre]Repository.java.
  - `/src/main/java/es/grupo8/backend/dto`: Objetos de transferencia de datos, para definir las estructuras de datos que se envían y reciben a través de la API.
  - `/src/main/java/es/grupo8/backend/entity`: Clases que representan las entidades de la base de datos, anotadas con JPA.

## 4. Estructura del Frontend
- **Arquitectura:** HTML/CSS/JS tradicional con una posible transición a React en las etapas finales del proyecto para mejorar la modularidad y la experiencia de desarrollo.
- **Estructura de directorios base:**
  - `/` : Todos los archivos .html de la aplicación en el directorio raiz.
  - `/css`: Todos los archivos .css de la aplicación.
  - `/javascript`: Todos los archivos .js de la aplicación.

## 5. Estructura de la Base de Datos
- **Tipo:** Relacional (PostgreSQL).
- **Convenciones de nomenclatura:**
  - Tablas: snake_case, plural [Ej: campaigns, campaign_types].
  - Columnas: snake_case, singular [Ej: id_chain, name].
- **Relaciones principales:**
  - **Campañas:** `campaign_types` (1:N) → `campaigns`
  - **Geografía:** `geographic_zones` (1:N) → `localities` (1:N) → `districts`, `postal_codes`
  - **Usuarios Base:** `user_accounts` (1:1) → `postal_codes`; se especializa en:
    - `administrators` (1:1)
    - `partner_entity_managers` (1:1) → `partner_entities`
    - `coordinators` (N:N) → `campaigns`
    - `captains` (N:N) → `campaigns`
  - **Tiendas y Cadenas:** `chains` (1:N) → `stores`; `stores` → `postal_codes`, `user_accounts` (responsable)
  - **Campaña y Tiendas:** `campaigns` (N:N) → `stores` (a través de `campaign_stores`)
  - **Voluntariado:** `partner_entities` (1:N) → `volunteers` (N:N) → `campaigns` (a través de `volunteer_shifts`)
  - **Turnos:** `volunteer_shifts` vincula `volunteers`, `campaigns` y `stores` (mediante `campaign_stores`) con fechas y horarios específicos


## 6. Guías, Buenas Prácticas y Rutas

### Convenciones Generales

- **Idioma:** Todo el código, comentarios y documentación deben estar en inglés. Prohibido el uso de otros idiomas en el código, comentarios o documentación.

### Backend

- **Convenciones de Código:** Tipado estricto obligatorio. Retornos y argumentos con tipos explícitos. camelCase para variables y métodos, PascalCase para clases y entidades.
- **Rutas y Endpoints:**
  ```java
  // API REST en controllers/rest/
  @RestController
  @RequestMapping("/api/partner-entities")

  // SSR en controllers/
  @Controller
  @GetMapping("/admin-partner-entities")
  ```
  - Prohibido rutas dinámicas sin patrón claro (ej: `/api/*`).
  - Los endpoints REST devuelven `ResponseEntity<T>`, nunca `ResponseEntity<?>`.
- **Servicios** (`services/`): Responsabilidad única. Prohibido mezclar lógica de negocio con acceso a datos o lógica de endpoints.
- **DTOs** (`dto/`): Obligatorio para comunicación entre capas y con el frontend. Prohibido exponer entidades JPA directamente en la API.
- **Controllers REST** (`controllers/rest/`): Delgados — solo reciben la petición, llaman al service y devuelven la respuesta. La autenticación se delega a `checkAuth()` (ver sección 12).
- **Controllers SSR** (`controllers/`): Cargan datos en `Model` y devuelven el nombre del JSP. Autenticación por sesión HTTP.
- **Repositorios** (`dao/`): Solo métodos para interactuar con BD. Prohibido lógica de negocio o validaciones.
- **Tests:** Obligatorios para services y controllers. Prohibido depender de BD o servicios externos (usar mocks).
- **Estructura General:** Cada módulo tiene sus entities, repositories, services, controllers y DTOs. Prohibido mezclar funcionalidades en un mismo módulo.
- **Manejo de Errores:** Usar excepciones personalizadas (`AuthException` en `exceptions/`, etc.). Prohibido usar `Exception` genérica para errores comunes.
- **Logging:** Solo por consola durante desarrollo, con mensajes claros y consistentes.
- **Paginación y filtrado:** Obligatorio usar `PaginatedResponse` (`dto/PaginatedResponse.java`) para listas paginadas. El filtrado y ordenación debe hacerse en BD con `Pageable` siempre que sea posible, no en memoria.

### Frontend

- **Convenciones de Código:** ES6+ obligatorio, `const`/`let`, funciones flecha para anónimas.
- **Rutas de Archivos:**
  ```
  /                     → archivos .html
  /css/                 → archivos .css
  /javascript/          → archivos .js
  /React/src/pages/     → componentes .jsx
  ```
  El nombre del archivo debe coincidir con la funcionalidad (ej: `admin-partner-entities.html`).
- **Llamadas a la API:** Usar `fetch` con el helper centralizado `authUtils.jsx`:
  ```jsx
  import { authHeaders } from "../auth/authUtils";

  // GET
  fetch(url, { headers: authHeaders() })

  // POST/PUT  
  fetch(url, {
      method: "POST",
      headers: authHeaders({ "Content-Type": "application/json" }),
      body: JSON.stringify(data)
  })
  ```
  Prohibido el uso de axios u otras librerías HTTP.
- **Estilos:** CSS exclusivamente, sin librerías de componentes.

### Implementaciones legacy

En esta etapa del proyecto hay bastantes funcionalidades y endpoints implementados tanto en frontend como en el backend. Para manejar código legacy:

1. Si el código legacy es de una funcionalidad **relacionada** con la tarea: revisarlo para que cumpla las guías y buenas prácticas definidas en este documento.
2. Si el código legacy **no está relacionado**: evitar modificarlo para no introducir errores. Prohibido tocarlo a menos que sea absolutamente necesario y con justificación clara en el pull request.

## 7. Flujo de trabajo git
- **Branching:** Uso de ramas para cada funcionalidad o bugfix, siguiendo la convención `feature/[nombre-funcionalidad]` o `bugfix/[descripcion-bug]`. Prohibido trabajar directamente en la rama main y dev.
- **Pull Requests:** Obligatorio crear un pull request para cada rama, con una descripción clara de los cambios realizados y la funcionalidad implementada. Prohibido fusionar ramas sin una revisión previa. El repositorio no permite pushear a main y a dev directamente, por lo que es obligatorio crear un pull request para fusionar cualquier rama a main o dev.
- **Revisiones de Código:** Cada pull request debe ser revisado por al menos un miembro del equipo antes de ser fusionado. Prohibido fusionar pull requests sin una revisión adecuada.
- **Commits:** Mensajes de commit claros y descriptivos.

## 8. Limitaciones de directorios de trabajo
- Debes de limitarte en la medida de lo posible a trabajar solo en los directorios relacionados con la funcionalidad que estes implementando, evitando modificar o tocar código de otras funcionalidades o módulos que no estén relacionados con tu tarea, para evitar conflictos y problemas de integración. Prohibido modificar código de otras funcionalidades o módulos que no estén relacionados con tu tarea, a menos que sea absolutamente necesario para la implementación de tu funcionalidad y siempre con una justificación clara en el pull request.
- En caso de que sea necesario modificar código de otras funcionalidades o módulos, se debe de comunicar previamente al equipo y obtener su aprobación antes de realizar cualquier cambio. Prohibido modificar código de otras funcionalidades o módulos sin una comunicación previa y una aprobación clara del equipo.

## 9. Variables de entorno
- Si por necesidad del proyecto se deben crear variables de entorno nuevas, estas deberan actualizarse tambien en el archivo .env.example con el mismo valor.

## 10. Uso opcional de definicion de requisitos con Markdown
- Si el desarrollador lo indica en la peticion a copilot, se leeran los requisitos y funcionalidades a implementar de un archivo .md de la carpeta requirements en la raiz del proyecto. Si esto es asi, el estado de la implementacion de la funcionalidad debera de actualizarse en dicho archivo, indicando partes de la implementacion pendientes, en proceso o completadas, en una seccion al final del archivo sin tocar los requisitos escritos previamente.

## 11. Patrones de Código Establecidos

### Backend — Autenticación en Controllers REST
- **Patrón `checkAuth()`**: Método privado en cada controller que valida el token JWT y el rol antes de ejecutar la lógica del endpoint. Lanza `AuthException` con el `HttpStatus` correspondiente (401 si token inválido, 403 si no tiene permisos).
  ```java
  private void checkAdmin(String auth) {
      Integer userId = authService.extractUserIdFromToken(auth);
      if (userId == null)
          throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
      if (!userService.isAdmin(userId))
          throw new AuthException(HttpStatus.FORBIDDEN, "No tienes permiso");
  }
  ```
- **`@RequestHeader`**: Usar siempre `@RequestHeader(value = "Authorization", required = false)` en los endpoints REST. Si el header falta, `checkAuth()` recibe `null` y devuelve un 401 JSON en lugar de un 400 HTML de Spring.
- **`@ExceptionHandler(AuthException.class)`**: Centralizado al final del controller, devuelve el status y mensaje de la excepción.
  ```java
  @ExceptionHandler(AuthException.class)
  public ResponseEntity<Map<String, String>> handleAuthException(AuthException e) {
      return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
  }
  ```
- **Tipado estricto**: Los endpoints deben devolver tipos concretos (`ResponseEntity<T>`), no `ResponseEntity<?>`. El uso de `AuthException` permite esto porque los casos de error se manejan en el `@ExceptionHandler`.
- **Clase `AuthException`**: Ubicada en `exceptions/AuthException.java`. Extiende `RuntimeException` y contiene `HttpStatus` para que el handler sepa qué código devolver.
- **Dos variantes de `checkAuth()` según el endpoint**:
  - `checkAdmin(auth)` — solo administradores (para listar, crear, eliminar)
  - `checkAdminOrEntityManager(auth, entityId)` — admin o manager de esa entidad (para obtener/editar)

### Backend — Mappers
- **Cada entidad tiene su mapper** en `mapper/` que extiende `MapperDTO<ResponseDTO, Entity>`.
  ```java
  @Component
  public class XxxMapper extends MapperDTO<XxxResponseDto, XxxEntity> {
      @Override
      public XxxResponseDto toDTO(XxxEntity entity) { ... }
  }
  ```
- Los services **nunca hacen mapeo manual**. Delegan en `mapper.toDTO()` y `mapper.toDTOList()`.
- `MapperDTO` proporciona `toDTOList()` y `toDtoSet()` de serie.

### Backend — Utilidades Compartidas
- **`UtilsService`** en `services/UtilsService.java` contiene métodos estáticos comunes:
  - `trimToNull(String)` — recorta y devuelve null si vacío
  - `normalizePhone(String)` — normaliza formato de teléfono
  - `normalizeEmail(String)` — normaliza email a minúsculas
  - `isValidEmail(String)`, `isValidPhone(String)`, `isValidPostalCode(String)` — validaciones
  - `hashPassword(String)`, `matchesPassword(...)`, `needsMigration(...)` — gestión de contraseñas BCrypt
  - `PHONE_PATTERN` — Pattern regex para teléfono
- **Prohibido duplicar** estas utilidades en los services. Si un service necesita `trimToNull()`, usa `UtilsService.trimToNull()`.

### Backend — Páginas SSR (JSP)
- **Autenticación por sesión**: Los endpoints `@Controller` que renderizan JSP usan `HttpSession` con `session.getAttribute("role")`. No usar `@RequestHeader("Authorization")`.
  ```jsp
  <%
      String role = (String) session.getAttribute("role");
      if (!"ADMINISTRADOR".equals(role)) {
          response.sendRedirect("/login");
          return;
      }
  %>
  ```
- **Datos en Model**: El controller carga los datos con `model.addAttribute()` y el JSP los renderiza con `<%= %>`.
- **Formularios CRUD**: Usar formularios HTML con `method="POST"` y redirects. No usar fetch/XHR para operaciones de escritura. Las acciones de crear/editar se envían por POST a un endpoint del controller y redirigen con flash attributes.
- **Eliminación**: Usar `POST` con `<form>` y confirmación con `onsubmit="return confirm(...)"`. Prohibido GET para eliminar.

### Frontend React — Autenticación
- **`authUtils.jsx`**: Archivo centralizado en `pages/auth/authUtils.jsx` con dos funciones:
  ```jsx
  export function getToken() {
      return sessionStorage.getItem("token");
  }

  export function authHeaders(extra = {}) {
      return {
          Authorization: `Bearer ${getToken()}`,
          ...extra
      };
  }
  ```
- **Todas las llamadas fetch** deben usar `authHeaders()`:
  ```jsx
  // GET
  fetch(url, { headers: authHeaders() })

  // POST/PUT
  fetch(url, {
      method: "POST",
      headers: authHeaders({ "Content-Type": "application/json" }),
      body: JSON.stringify(data)
  })

  // DELETE
  fetch(url, { method: "DELETE", headers: authHeaders() })
  ```
- **Prohibido** construir `Authorization: Bearer` manualmente o leer `sessionStorage.getItem('token')` directamente en los componentes.
- **Prohibido** tener funciones `getAuthToken()` locales en cada componente.

### Frontend React — Convenciones de Archivos
- **`.jsx`** para archivos que contienen JSX (componentes): `AdminChains.jsx`, `Login.jsx`
- **`.jsx`** también para utilidades y hooks aunque no tengan JSX: `authUtils.jsx`, `useAuthHook.jsx`
- No usar `.js` para archivos en el frontend React. Todo es `.jsx` para consistencia.