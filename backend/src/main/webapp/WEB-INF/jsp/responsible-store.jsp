<%--
    Pagina de detalle de tienda del responsable.

    Autores:
    - Alejandra Ortiz: 100%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String token = (String) session.getAttribute("token");
    String role = (String) session.getAttribute("role");

    if (token == null || !"RESPONSABLE_TIENDA".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Mi Tienda</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/responsible-store.css">
</head>
<body>

<header class="topbar">
    <a class="brand" href="/index" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Responsable" : nombre %></span>
        </div>
        <button class="btn-edit" id="btn-edit">Editar perfil 🖉</button>
        <button class="btn-logout" id="btn-logout">Cerrar sesión ×</button>
    </div>
</header>

<main class="page-wrapper" aria-label="Responsible store page">
    <div class="page-header">
        <h1>Mi tienda</h1>
        <p>Información de tu tienda asignada y turnos programados.</p>
    </div>

    <div class="card hidden" id="card-tienda">
        <div class="card-header"><h2 id="store-title">Cargando...</h2></div>
        <div class="info-grid" id="info-grid"></div>
    </div>

    <div class="card hidden" id="card-turnos">
        <div class="card-header"><h2>Turnos programados</h2></div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Campaña</th>
                        <th>Voluntario</th>
                        <th>Hora fin</th>
                        <th>Asistencia</th>
                        <th>Notas</th>
                    </tr>
                </thead>
                <tbody id="shifts-tbody">
                    <tr><td colspan="5" class="table-empty">Cargando turnos...</td></tr>
                </tbody>
            </table>
        </div>
    </div>

    <div id="error-msg" class="error-panel hidden"></div>
</main>

<div class="toast-container" id="toast-container"></div>

<script>
    (function () {
        var token = '<%= token == null ? "" : token %>';
        var storeId = '<%= session.getAttribute("storeId") == null ? "" : session.getAttribute("storeId") %>';

        document.getElementById("btn-edit").addEventListener("click", function () {
            window.location.href = "/edit";
        });
        document.getElementById("btn-logout").addEventListener("click", function () {
            window.location.href = "/logout";
        });

        if (!token) { window.location.href = "/login"; return; }

        function authHeaders() {
            return { "Content-Type": "application/json", "Authorization": "Bearer " + token };
        }

        function showError(msg) {
            var el = document.getElementById("error-msg");
            el.textContent = msg;
            el.classList.remove("hidden");
        }

        function renderStoreInfo(store) {
            document.getElementById("store-title").textContent = store.name || "Tienda";
            document.getElementById("card-tienda").classList.remove("hidden");

            var fields = [
                { label: "Nombre",        value: store.name       },
                { label: "Domicilio",     value: store.address    },
                { label: "Código postal", value: store.postalCode },
                { label: "Localidad",     value: store.locality   },
                { label: "Zona geog.",    value: store.zone       },
                { label: "Cadena",        value: store.chainName  }
            ];

            var infoGrid = document.getElementById("info-grid");
            infoGrid.innerHTML = "";
            fields.forEach(function (f) {
                var item = document.createElement("div");
                item.className = "info-item";

                var label = document.createElement("label");
                label.textContent = f.label;
                item.appendChild(label);

                var span = document.createElement("span");
                span.textContent = f.value || "\u2014";
                item.appendChild(span);

                infoGrid.appendChild(item);
            });
        }

        function renderShifts(shifts) {
            document.getElementById("card-turnos").classList.remove("hidden");
            var tbody = document.getElementById("shifts-tbody");

            if (!shifts || !shifts.length) {
                tbody.innerHTML = "";
                var tr = document.createElement("tr");
                var td = document.createElement("td");
                td.colSpan = 5; td.className = "table-empty";
                td.textContent = "No hay turnos programados.";
                tr.appendChild(td); tbody.appendChild(tr);
                return;
            }

            tbody.innerHTML = "";
            shifts.forEach(function (s) {
                var tr = document.createElement("tr");

                var td0 = document.createElement("td"); td0.textContent = s.campaignName || "\u2014"; tr.appendChild(td0);
                var td1 = document.createElement("td"); td1.textContent = s.volunteerName || "\u2014"; tr.appendChild(td1);
                var td2 = document.createElement("td"); td2.textContent = s.endTime || "\u2014"; tr.appendChild(td2);

                var td3 = document.createElement("td");
                var attendanceBadge = document.createElement("span");
                if (s.attendance === true) {
                    attendanceBadge.className = "badge-attendance badge-yes";
                    attendanceBadge.textContent = "\u2713 S\u00ed";
                } else if (s.attendance === false) {
                    attendanceBadge.className = "badge-attendance badge-no";
                    attendanceBadge.textContent = "\u2717 No";
                } else {
                    attendanceBadge.className = "badge-attendance badge-pending";
                    attendanceBadge.textContent = "Pendiente";
                }
                td3.appendChild(attendanceBadge); tr.appendChild(td3);

                var td4 = document.createElement("td"); td4.textContent = s.notes || "\u2014"; tr.appendChild(td4);

                tbody.appendChild(tr);
            });
        }

        function loadStoreDetail() {
            if (!storeId) {
                showError("No tienes ninguna tienda asignada. Contacta con el administrador.");
                return;
            }

            fetch("/api/stores/" + storeId + "/detail", { headers: authHeaders() })
                .then(function (r) {
                    if (r.status === 401) { window.location.href = "/login"; return null; }
                    if (r.status === 403) { showError("No tienes permiso para ver esta tienda."); return null; }
                    if (r.status === 404) { showError("Tienda no encontrada."); return null; }
                    if (!r.ok) { showError("Error al cargar la información de la tienda."); return null; }
                    return r.json();
                })
                .then(function (data) {
                    if (!data) return;
                    renderStoreInfo(data);
                    renderShifts(data.scheduledShifts);
                })
                .catch(function () {
                    showError("Error de conexión con el servidor.");
                });
        }

        loadStoreDetail();
    }());
</script>
</body>
</html>
