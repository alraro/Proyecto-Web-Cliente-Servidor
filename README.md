# Proyecto Web Cliente-Servidor

<p align="center">
	<strong>Trabajo de Desarrollo Web (Cliente + Servidor)</strong><br>
	<strong>Grupo 8</strong>
</p>

<p align="center">
	Este repositorio reúne la parte de cliente y la parte de servidor en un mismo entorno,
	con despliegue unificado mediante Docker y base de datos PostgreSQL.
</p>

---

## Stack tecnológico

<p align="center">
	<img alt="HTML5" src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white" />
	<img alt="CSS3" src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white" />
	<img alt="JavaScript" src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=111" />
	<img alt="Java" src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white" />
	<img alt="Spring Boot" src="https://img.shields.io/badge/Spring_Boot-4.0.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
	<img alt="Maven" src="https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />
	<img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
	<img alt="Docker" src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
	<img alt="Nginx" src="https://img.shields.io/badge/Nginx-Frontend-009639?style=for-the-badge&logo=nginx&logoColor=white" />
</p>

---

## Equipo (Grupo 8)

<table align="center">
  <tr>
    <td align="center">
      <a href="https://github.com/alraro">
        <img src="https://avatars.githubusercontent.com/alraro?v=4" width="100px;" alt="Alfonso Ramos"/>
        <br /><sub><b>Alfonso Ramos</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/alejandraortiz05">
        <img src="https://avatars.githubusercontent.com/alejandraortiz05?v=4" width="100px;" alt="Alejandra Ortiz"/>
        <br /><sub><b>Alejandra Ortiz</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/alexcalvo0101">
        <img src="https://avatars.githubusercontent.com/alexcalvo0101?v=4" width="100px;" alt="Alejandro Calvo"/>
        <br /><sub><b>Alejandro Calvo</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/Chewi9">
        <img src="https://avatars.githubusercontent.com/Chewi9?v=4" width="100px;" alt="Hugo Herrero"/>
        <br /><sub><b>Hugo Herrero</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/fernan92005">
        <img src="https://avatars.githubusercontent.com/fernan92005?v=4" width="100px;" alt="Fernando Luis Pinilla"/>
        <br /><sub><b>Fernando Luis Pinilla</b></sub>
      </a>
    </td>
  </tr>
</table>

---

## Estructura breve del proyecto

```text
Proyecto-Web-Cliente-Servidor/
├─ frontend/     -> Cliente estático (HTML, CSS, JS) servido con Nginx
├─ backend/      -> Aplicación Spring Boot (SSR con JSP + API REST)
├─ database/     -> Script init.sql para inicializar PostgreSQL
└─ docker-compose.yml -> Orquestación completa (frontend, backend, db, adminer)
```

---

## Despliegue rápido con Docker

### 1) Requisitos

- Docker
- Docker Compose

### 2) Levantar todo el entorno

```bash
docker compose up -d --build
```

### 3) Accesos principales

- Frontend: http://localhost:80
- Backend SSR (JSP): http://localhost:8080/inicio
- Backend API REST: http://localhost:8080/api/v1/ejemplo
- Adminer (gestor web de BD): http://localhost:8081

### 4) Parar servicios

```bash
docker compose down
```

Si además quieres borrar volúmenes de datos:

```bash
docker compose down -v
```

---

## ¿Qué hace cada servicio?

- frontend: publica la parte cliente en Nginx.
- backend: expone SSR y endpoints REST.
- database: PostgreSQL 15 con persistencia en volumen Docker.
- adminer: interfaz web para inspeccionar y gestionar la base de datos.

---

## Guía sencilla para trabajar Backend en IntelliJ (local)

Esta guía permite desarrollar el backend con hot reload en IntelliJ, usando PostgreSQL en Docker.

### 1) Levantar solo base de datos + Adminer

```bash
docker compose up -d database adminer
```

### 2) Configurar variables de entorno del backend en IntelliJ

En la Run Configuration de Spring Boot añade estas variables (ajusta valores según tu `.env`):

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tu_base_de_datos
SPRING_DATASOURCE_USERNAME=tu_usuario
SPRING_DATASOURCE_PASSWORD=tu_password
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

Notas:

- Usa `localhost` (no `database`) al ejecutar backend fuera de Docker.
- Mantén el puerto publicado de PostgreSQL en `5432` salvo que lo cambies en `.env`.

### 3) Ejecutar backend desde IntelliJ

- Abre el módulo `backend` como proyecto Maven.
- Espera la carga de dependencias.
- Ejecuta la clase principal `BackendApplication`.

### 4) Verificar funcionamiento

- SSR: http://localhost:8080/inicio
- API: http://localhost:8080/api/v1/ejemplo
- Adminer: http://localhost:8081

### 5) Flujo recomendado de trabajo

1. Arranca `database` y `adminer` con Docker.
2. Ejecuta `backend` en IntelliJ para desarrollar.
3. Si necesitas validar integración completa, levanta también `frontend` y `backend` por Docker.

---

## Estado del proyecto

Estado actual (lo que ya existe):

- Backend único en Spring Boot que centraliza la lógica de negocio, acceso a datos y endpoints API.
- Renderizado SSR con JSP para páginas base de acceso (`/`, `/login`, `/register`).
- Frontend estático en `frontend/` con HTML, CSS y JavaScript servido por Nginx.
- Base de datos PostgreSQL y entorno de ejecución unificado con Docker Compose.

Dirección definida del proyecto (sin entrar en implementación futura):

- Mantener un único backend común.
- Conectar ese backend con tres variantes de frontend: JSP, HTML/CSS/JavaScript y React.
