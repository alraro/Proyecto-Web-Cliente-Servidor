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
  - `/src/main/java/es/grupo8/backend/repositories`: Repositorios que interactúan directamente con la base de datos, utilizando JPA/Hibernate.
  - `/src/main/java/es/grupo8/backend/config`: Configuraciones generales del proyecto, como seguridad, CORS, etc.
  - `/src/main/java/es/grupo8/backend/dao`: Objetos de acceso a datos, si se utilizan para mapear consultas personalizadas o vistas.
  - `/src/main/java/es/grupo8/backend/dto`: Objetos de transferencia de datos, para definir las estructuras de datos que se envían y reciben a través de la API.
  - `/src/main/java/es/grupo8/backend/entity`: Clases que representan las entidades de la base de datos, anotadas con JPA.
  - `/src/main/java/es/grupo8/backend/security`: Configuraciones y clases relacionadas con la seguridad, como filtros, proveedores de autenticación, etc.

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
### Backend
- **Convenciones de Código:** Tipado estricto obligatorio. Retornos de funciones explícitos. Argumentos de funciones con tipos claros y tabulados multinivel si son muchos y muy largos.
- **Rutas y Endpoints:** 
    - Para enpoints de API: /api/[recurso]/[acción] (ej: /api/partner-entity-managers/:id). Para endpoints SSR: /[recurso]/[acción] (ej: /campaigns/list).
    - Prohibido el uso de rutas dinámicas sin un patrón claro (ej: /api/*).
    - **Servicios:** Carpeta services. Cada servicio debe tener una responsabilidad clara y única. Prohibido mezclar lógica de negocio con lógica de acceso a datos con logica de endpoints.
    - **DTOs:** Carpeta dao. Obligatorio el uso de DTOs para la comunicación entre capas y con el frontend. Prohibido exponer entidades directamente a través de la API.
    - **Controllers:** Deben ser delgados, delegando la mayor parte de la lógica a los servicios. Prohibido incluir lógica de negocio o acceso a datos en los controladores, y manejar casos de error.
    - **Repositorios:** Solo deben contener métodos para interactuar con la base de datos. Prohibido incluir lógica de negocio o validaciones en los repositorios.
    - **Tests:** Obligatorio el uso de pruebas unitarias para servicios y controladores, con una cobertura mínima del 80%. Prohibido escribir pruebas que dependan de la base de datos o de servicios externos (mockear siempre).
    - **DAO:** Deben contener métodos para interactuar con la base de datos. Prohibido incluir lógica de negocio o validaciones en los DAOs.
- **Estructura General:** Cada módulo o funcionalidad debe de tener sus entities, Repositories, Services, Controllers y DTOs correspondientes, evitando mezclar funcionalidades en un mismo módulo.
- **Documentacion:** Cada clase y método debe tener una documentación clara y concisa que explique su propósito, parámetros y retorno. Prohibido dejar código sin documentar o con documentación vaga o incompleta. 
- **Manejo de Errores:** Uso de excepciones personalizadas para casos específicos (ej: `EntityNotFoundException`, `ValidationException`). Prohibido el uso de excepciones genéricas (`Exception`) para manejar errores comunes.
- **Logging:** Logging por consola simplemente para desarrollo, con mensajes claros y consistentes.

### Frontend
- **Convenciones de Código:** Uso de ES6+ obligatorio. Prohibido el uso de var, preferencia por const y let. Funciones flecha para funciones anónimas.
- **Rutas de Archivos:** Los archivos html deben de estar en la raiz de la carpeta del frontend y los archivos css y js en sus respectivas carpetas. Prohibido mezclar archivos html, css y js en el mismo directorio. Comunicación con el backend a través de endpoints RESTful definidos en el backend, usando rutas claras y consistentes (ej: `/api/partner-entity-managers/:id`). El nombre de los archivos html, js y css debe coincidir y ser claro con la funcionalidad que implementan (ej: admin-partner-entities.html, admin-partner-entities.js, admin-partner-entities.css).
- **Llamadas a la API:** Uso de `fetch` para llamadas a la API, con manejo adecuado de errores y respuestas. Prohibido el uso de librerías externas para llamadas a la API (ej: axios) en esta etapa del proyecto.
- **Estilos:** Uso exclusivo de `CSS`.

- **Documentación:** Cada archivo debe tener un comentario al inicio que explique su propósito y cualquier dependencia o relación con otros archivos. Prohibido dejar archivos sin documentación o con documentación vaga o incompleta.

- **Implementaciones legacy:** En esta etapa del proyecto ya hay bastantes funcionalidades y endpoints implementados tanto en frontend como en el backend, por lo que si se tiene que manejar codigo legacy, se debera aprovechar para refactorizarlo y mejorarlo, siguiendo las mismas reglas y convenciones que el nuevo código. Prohibido dejar código legacy que usemos sin refactorizar o con malas prácticas, aunque funcione correctamente.

## 7. Flujo de trabajo git
- **Branching:** Uso de ramas para cada funcionalidad o bugfix, siguiendo la convención `feature/[nombre-funcionalidad]` o `bugfix/[descripcion-bug]`. Prohibido trabajar directamente en la rama main y dev.
- **Pull Requests:** Obligatorio crear un pull request para cada rama, con una descripción clara de los cambios realizados y la funcionalidad implementada. Prohibido fusionar ramas sin una revisión previa. El repositorio no permite pushear a main y a dev directamente, por lo que es obligatorio crear un pull request para fusionar cualquier rama a main o dev.
- **Revisiones de Código:** Cada pull request debe ser revisado por al menos un miembro del equipo antes de ser fusionado. Prohibido fusionar pull requests sin una revisión adecuada.
- **Commits:** Mensajes de commit claros y descriptivos.

## 8. Limitaciones de directorios de trabajo
- Debes de limitarte en la medida de lo posible a trabajar solo en los directorios relacionados con la funcionalidad que estes implementando, evitando modificar o tocar código de otras funcionalidades o módulos que no estén relacionados con tu tarea, para evitar conflictos y problemas de integración. Prohibido modificar código de otras funcionalidades o módulos que no estén relacionados con tu tarea, a menos que sea absolutamente necesario para la implementación de tu funcionalidad y siempre con una justificación clara en el pull request.
- En caso de que sea necesario modificar código de otras funcionalidades o módulos, se debe de comunicar previamente al equipo y obtener su aprobación antes de realizar cualquier cambio. Prohibido modificar código de otras funcionalidades o módulos sin una comunicación previa y una aprobación clara del equipo.