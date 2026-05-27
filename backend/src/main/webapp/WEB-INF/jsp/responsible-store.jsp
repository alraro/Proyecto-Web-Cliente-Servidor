<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Mi Tienda</title>
    <link rel="stylesheet" href="/css/administrador.css">
    <link rel="stylesheet" href="/css/admin-validate-responsible.css">
</head>
<body>

<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/index" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-actions">
        <span id="user-name">Responsable</span>
        <a class="btn" href="/edit">Editar perfil</a>
        <button type="button" id="btn-logout" class="btn">Cerrar sesión</button>
    </div>
</header>

<main class="admin-page" aria-label="Responsible store page">
    <section class="page-header">
        <h1>Mi tienda</h1>
        <p>Información de tu tienda asignada y turnos programados.</p>
    </section>

    <section class="card hidden" id="card-tienda" aria-label="Información de la tienda">
        <h2 id="store-title">Cargando...</h2>
        <div class="info-grid" id="info-grid"></div>
    </section>

    <section class="card hidden section-gap" id="card-turnos" aria-label="Turnos programados">
        <h2>Turnos programados</h2>
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
    </section>

    <div id="error-msg" class="error-panel hidden"></div>
</main>

<script>
    (function () {
        var token = localStorage.getItem("token");
        var role  = localStorage.getItem("role");

        document.getElementById("user-name").textContent = localStorage.getItem("nombre") || "Responsable";
        document.getElementById("btn-logout").addEventListener("click", function () {
            localStorage.clear(); window.location.href = "/login";
        });

        if (!token || role !== "RESPONSABLE_TIENDA") { window.location.href = "/login"; return; }

        function authHeaders() {
            return { "Content-Type": "application/json", "Authorization": "Bearer " + token };
        }

        function showError(msg) {
            var el = document.getElementById("error-msg");
            el.textContent = msg; el.classList.remove("hidden");
        }

        function renderStoreInfo(store) {
            document.getElementById("store-title").textContent = store.name || "Tienda";
            document.getElementById("card-tienda").classList.remove("hidden");

            var fields = [
                { label: "Nombre",        value: store.name       },
                { label: "Domicilio",     value: store.address    },
                { label: "C\u00f3digo postal", value: store.postalCode },
                { label: "Localidad",     value: store.locality   },
                { label: "Zona geog.",    value: store.zone       },
                { label: "Cadena",        value: store.chainName  }
            ];

            var grid = document.getElementById("info-grid");
            grid.innerHTML = "";
            fields.forEach(function (f) {
                var item = document.createElement("div");
                item.className = "info-item";
                item.innerHTML = "<label>" + f.label + "</label><span>" + (f.value || "\u2014") + "</span>";
                grid.appendChild(item);
            });
        }

        function renderShifts(shifts) {
            document.getElementById("card-turnos").classList.remove("hidden");
            var tbody = document.getElementById("shifts-tbody");
            tbody.innerHTML = "";

            if (!shifts || !shifts.length) {
                tbody.innerHTML = '<tr><td colspan="5" class="table-empty">No hay turnos programados.</td></tr>';
                return;
            }

            shifts.forEach(function (s) {
                var badgeClass, badgeText;
                if (s.attendance === true)  { badgeClass = "badge-attendance badge-yes";     badgeText = "\u2713 S\u00ed"; }
                else if (s.attendance === false) { badgeClass = "badge-attendance badge-no"; badgeText = "\u2717 No"; }
                else                        { badgeClass = "badge-attendance badge-pending"; badgeText = "Pendiente"; }

                var tr = document.createElement("tr");
                tr.innerHTML =
                    "<td>" + (s.campaignName  || "\u2014") + "</td>" +
                    "<td>" + (s.volunteerName || "\u2014") + "</td>" +
                    "<td>" + (s.endTime       || "\u2014") + "</td>" +
                    "<td><span class='" + badgeClass + "'>" + badgeText + "</span></td>" +
                    "<td>" + (s.notes         || "\u2014") + "</td>";
                tbody.appendChild(tr);
            });
        }

        function loadStoreDetail() {
            var storeId = localStorage.getItem("storeId");
            if (!storeId) { showError("No tienes ninguna tienda asignada. Contacta con el administrador."); return; }

            fetch("/api/stores/" + storeId + "/detail", { headers: authHeaders() })
                .then(function (r) {
                    if (r.status === 401) { localStorage.clear(); window.location.href = "/login"; return null; }
                    if (r.status === 403) { showError("No tienes permiso para ver esta tienda."); return null; }
                    if (r.status === 404) { showError("Tienda no encontrada."); return null; }
                    if (!r.ok)            { showError("Error al cargar la información de la tienda."); return null; }
                    return r.json();
                })
                .then(function (data) {
                    if (!data) return;
                    renderStoreInfo(data);
                    renderShifts(data.scheduledShifts);
                })
                .catch(function () { showError("Error de conexión con el servidor."); });
        }

        loadStoreDetail();
    }());
</script>
</body>
</html>
