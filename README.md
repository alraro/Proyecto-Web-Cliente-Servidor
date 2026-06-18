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
├─ frontend/     -> Cliente estático (HTML, CSS, JS) y React servido con Nginx
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

- Frontend HTML, CSS y JavaScript: http://localhost:80
- Frontend React: http://localhost:3000
- Backend SSR (JSP): http://localhost:8080
- Backend API REST: http://localhost:8080/api/
- Adminer (gestor web de BD): http://localhost:8081

### 4) Parar servicios

```bash
docker compose down
```

Si además quieres borrar volúmenes de datos:

```bash
docker compose down -v
```