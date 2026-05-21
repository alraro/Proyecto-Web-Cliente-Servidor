# Feature: Módulo Responsable de Entidad Colaboradora

## Contexto del proyecto

Proyecto full-stack de administración para **BancoSol** (banco de alimentos).
- **Backend:** Spring Boot
- **Frontend:** React
- El esquema completo de la base de datos está en `agents.workflow.macdown`.
- Separación estricta de responsabilidades: HTML, JS y CSS en archivos independientes. **Nunca** mezclar CSS ni JS dentro de HTML.

---

## Rol a implementar: `RESPONSABLE_ENTIDAD_COLABORADORA`

Este usuario gestiona el voluntariado vinculado a **su propia entidad colaboradora**.

### Acciones permitidas sobre Voluntarios
- **Crear** un voluntario
- **Editar** un voluntario
- **Eliminar** un voluntario
- **Consultar/listar** los voluntarios de su entidad

### Reglas de negocio
- Un voluntario pertenece a **una entidad colaboradora**.
- Un voluntario puede estar asociado a **una o varias campañas**.
- El responsable **solo puede operar sobre voluntarios de su propia entidad** (nunca de otras).

---

## Backend — Spring Boot

### Estructura de archivos a crear/modificar

```
src/main/java/.../
├── controller/
│   └── VoluntarioController.java        ← NUEVO o modificar si existe
├── service/
│   └── VoluntarioService.java           ← NUEVO o modificar si existe
├── repository/
│   └── VoluntarioRepository.java        ← NUEVO o modificar si existe
├── model/
│   └── Voluntario.java                  ← NUEVO o modificar si existe
└── dto/
    ├── VoluntarioRequestDTO.java         ← NUEVO
    └── VoluntarioResponseDTO.java        ← NUEVO
```

> Reutilizar los endpoints ya existentes de `EntidadColaboradora` si los hay.

---

### Convenciones de implementación

#### Controller
- **Solo** recibe la petición HTTP, parsea parámetros y comprueba autenticación/autorización.
- Recibe parámetros con `@RequestParam` (no `@PathVariable` ni `@RequestBody` a menos que sea estrictamente necesario).
- Delega toda la lógica al Service. **No llama directamente al Repository.**
- Anotaciones básicas: `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@RequestParam`, `@RequestBody`.

```java
// Ejemplo de firma de método en el Controller
@GetMapping("/voluntarios")
public ResponseEntity<List<VoluntarioResponseDTO>> listarVoluntarios(@RequestParam Long entidadId) {
    // 1. Comprobar que el usuario autenticado es responsable de esa entidad
    // 2. Llamar al service
    // 3. Devolver respuesta
}
```

#### Service
- Contiene **toda la lógica de negocio**.
- Usa el Repository para acceder a datos. El Controller **nunca** accede al Repository directamente.
- Métodos claros y cortos, uno por caso de uso (listar, crear, editar, eliminar).

#### Repository
- Extiende `JpaRepository<Voluntario, Long>`.
- Añadir solo los métodos de consulta necesarios (p.ej. `findByEntidadId`).

#### Model / Entity
- Anotaciones básicas: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@ManyToOne`, `@ManyToMany`.
- Tipos de datos simples: `Long`, `String`, `LocalDate`. Nada de tipos exóticos.

#### DTOs
- `VoluntarioRequestDTO`: datos que llegan desde el frontend (crear/editar).
- `VoluntarioResponseDTO`: datos que se devuelven al frontend.
- Usar solo atributos con getters/setters estándar. Sin lógica interna.

---

### Endpoints a exponer

| Método | URL | Descripción |
|--------|-----|-------------|
| `GET` | `/api/voluntarios?entidadId={id}` | Listar voluntarios de una entidad |
| `POST` | `/api/voluntarios` | Crear voluntario |
| `PUT` | `/api/voluntarios/{id}` | Editar voluntario |
| `DELETE` | `/api/voluntarios/{id}` | Eliminar voluntario |

---

## Frontend — React

### Estructura de archivos a crear

```
src/
└── pages/
    └── responsable/
        ├── GestionVoluntarios.jsx       ← Página principal
        └── GestionVoluntarios.css       ← Estilos exclusivos de esta página
```

> Si ya existe un componente reutilizable de cabecera (`Header`) o pie de página (`Footer`), importarlo. Si no existe, crearlo como componente separado.

---

### Convenciones de implementación

#### JSX / React
- Componentes funcionales con hooks (`useState`, `useEffect`).
- **Separación de responsabilidades:** lógica de llamadas a la API en funciones independientes (no dentro del JSX ni mezclado con el render).
- Funciones pequeñas y con nombre descriptivo. Sin funciones monolíticas ni "espagueti".
- Etiquetas HTML básicas únicamente: `<div>`, `<table>`, `<tr>`, `<td>`, `<th>`, `<input>`, `<button>`, `<form>`, `<label>`, `<select>`, `<p>`, `<h2>`, etc.

```jsx
// Estructura sugerida del componente
function GestionVoluntarios() {
  // 1. Estado
  // 2. Llamadas a la API (funciones separadas)
  // 3. Handlers de eventos (funciones separadas)
  // 4. Return con JSX limpio
}
```

#### CSS
- Estilos en archivo `.css` propio del componente. **Nunca** estilos inline en el JSX.
- Seguir el mismo formato visual que el resto del proyecto: **tabla con columnas** (nombre, acciones, etc.), botones de editar/eliminar por fila.
- Interfaz simple, funcional y legible. Sin animaciones ni estilos complejos.

---

### Pantalla a implementar: Gestión de Voluntarios

**Elementos de la página:**

1. **Cabecera** — Reutilizar el componente de cabecera existente del proyecto.
2. **Título** — "Voluntarios de [Nombre Entidad]"
3. **Tabla de voluntarios** con columnas:
   - Nombre
   - Apellidos
   - DNI / identificador
   - Campañas asociadas (listado o badge)
   - Acciones: botón **Editar** + botón **Eliminar**
4. **Botón "Añadir Voluntario"** — abre un formulario (modal o sección inline).
5. **Formulario de creación/edición** con campos:
   - Nombre
   - Apellidos
   - DNI / identificador
   - Selección de campaña(s)
   - Botones: **Guardar** / **Cancelar**
6. **Footer** — Reutilizar el componente de pie de página existente.

---

## Restricciones globales

| ❌ Prohibido | ✅ Correcto |
|---|---|
| CSS o JS dentro del HTML/JSX | Archivos `.css` y `.jsx` separados |
| Lógica de negocio en el Controller | Lógica de negocio solo en el Service |
| Controller llamando al Repository directamente | Controller → Service → Repository |
| Tipos de datos exóticos en las entidades | `String`, `Long`, `LocalDate`, etc. |
| Anotaciones Spring poco comunes | Solo anotaciones estándar y conocidas |
| Etiquetas HTML complejas o poco comunes | Solo etiquetas HTML básicas |
| Funciones monolíticas o espagueti | Funciones pequeñas con nombre descriptivo |
| Tests | No son necesarios |

---

## Prioridad de instrucciones

> Si existe cualquier conflicto entre este documento y otros ficheros del proyecto (p.ej. `agents.workflow.macdown`), **este documento tiene prioridad**.

# Entorno actual

Además, si tienen necesidad de probar el código antes de decir que has terminado y tal creo que el reacta ahora mismo no está puesto para que funcione con Docker compose, si es así, tú tienes también que crear un nuevo Docker file meterlo en la carpeta de RIAC que compila el proyecto de reactital o cómo funcione para que se despliegue también con el mismo comando Docker compost up menos menos build como hacen el resto del proyecto ahora mismo.