<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Validar registros</title>
    <link rel="stylesheet" href="/css/administrador.css">
    <link rel="stylesheet" href="/css/admin-validate-responsible.css">
</head>
<body>

<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/index" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-actions">
        <span id="user-name">Admin</span>
        <a class="btn" href="/edit">Editar perfil</a>
        <button type="button" id="btn-logout" class="btn">Cerrar sesión</button>
    </div>
</header>

<main class="admin-page" aria-label="Validate users page">
    <section class="page-header">
        <a href="/admin" class="back-link">&larr; Volver al menú</a>
        <h1>Validar registros</h1>
        <p>Revisa y aprueba o rechaza las solicitudes de nuevos usuarios.</p>
    </section>

    <div id="global-message" hidden></div>

    <section class="card" aria-label="Usuarios pendientes">
        <div class="card-header-row">
            <h2>
                Pendientes de aprobación
                <span id="badge-pending" class="badge-count hidden"></span>
            </h2>
            <button type="button" id="btn-refresh-pending" class="btn-refresh">↻ Actualizar</button>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Email</th>
                        <th>Teléfono</th>
                        <th>Asignar rol</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody id="pending-tbody">
                    <tr><td colspan="6" class="table-empty">Cargando...</td></tr>
                </tbody>
            </table>
        </div>
    </section>
</main>

<script>
    (function () {
        var token = localStorage.getItem("token");
        var role  = localStorage.getItem("role");

        document.getElementById("user-name").textContent = localStorage.getItem("nombre") || "Admin";
        document.getElementById("btn-logout").addEventListener("click", function () {
            localStorage.clear(); window.location.href = "/login";
        });

        if (!token || role !== "ADMINISTRADOR") { window.location.href = "/login"; return; }

        function authHeaders() {
            return { "Content-Type": "application/json", "Authorization": "Bearer " + token };
        }

        function escHtml(v) {
            return String(v == null ? "" : v)
                .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
        }

        function loadPending() {
            var tbody = document.getElementById("pending-tbody");
            tbody.innerHTML = '<tr><td colspan="6" class="table-empty">Cargando...</td></tr>';

            fetch("/api/users/pending", { headers: authHeaders() })
                .then(function (r) {
                    if (r.status === 401 || r.status === 403) { localStorage.clear(); window.location.href = "/login"; return null; }
                    if (!r.ok) throw new Error();
                    return r.json();
                })
                .then(function (data) {
                    if (!data) return;
                    var badge = document.getElementById("badge-pending");
                    if (data.length > 0) { badge.textContent = data.length; badge.classList.remove("hidden"); }
                    else { badge.classList.add("hidden"); }

                    tbody.innerHTML = "";
                    if (!data.length) {
                        tbody.innerHTML = '<tr><td colspan="6" class="table-empty">No hay usuarios pendientes de aprobación.</td></tr>';
                        return;
                    }

                    data.forEach(function (u) {
                        var tr = document.createElement("tr");

                        var roleOpts = [
                            { value: "",                   label: "Seleccionar rol..."    },
                            { value: "ADMINISTRADOR",      label: "Administrador"         },
                            { value: "COORDINADOR",        label: "Coordinador"           },
                            { value: "CAPITAN",            label: "Capit\u00e1n"          },
                            { value: "COLABORADOR",        label: "Colaborador"           },
                            { value: "RESPONSABLE_TIENDA", label: "Responsable de Tienda" }
                        ];
                        var selectHtml = '<select id="role-' + u.id + '" class="role-select">';
                        roleOpts.forEach(function (r) {
                            selectHtml += '<option value="' + r.value + '">' + r.label + '</option>';
                        });
                        selectHtml += '</select>';

                        tr.innerHTML =
                            "<td>" + u.id + "</td>" +
                            "<td><strong>" + escHtml(u.name) + "</strong></td>" +
                            "<td>" + escHtml(u.email || "\u2014") + "</td>" +
                            "<td>" + escHtml(u.phone || "\u2014") + "</td>" +
                            "<td>" + selectHtml + "</td>" +
                            "<td><div class='td-actions'>" +
                                "<button class='btn btn-primary btn-sm' data-action='approve' data-user-id='" + u.id + "'>\u2713 Aprobar</button>" +
                                "<button class='btn-danger btn-sm'     data-action='reject'  data-user-id='" + u.id + "' data-user-name='" + escHtml(u.name) + "'>\u2717 Rechazar</button>" +
                            "</div></td>";
                        tbody.appendChild(tr);
                    });
                })
                .catch(function () {
                    document.getElementById("pending-tbody").innerHTML =
                        '<tr><td colspan="6" class="table-empty">Error al conectar con el servidor.</td></tr>';
                });
        }

        function approveUser(id) {
            var sel = document.getElementById("role-" + id);
            var roleVal = sel ? sel.value : "";
            if (!roleVal) { showMessage("Selecciona un rol antes de aprobar.", true); return; }
            fetch("/api/users/" + id + "/role", { method: "POST", headers: authHeaders(), body: JSON.stringify({ role: roleVal }) })
                .then(function (r) { return r.json().then(function (d) { return { ok: r.ok, data: d }; }); })
                .then(function (r) {
                    if (!r.ok) { showMessage(r.data.message || "Error al asignar rol.", true); return; }
                    showMessage("Usuario aprobado como " + roleVal + ".", false); loadPending();
                })
                .catch(function () { showMessage("Error de conexión.", true); });
        }

        function rejectUser(id, name) {
            if (!confirm("¿Rechazar y eliminar la cuenta de \"" + name + "\"?\nEsta acción no se puede deshacer.")) return;
            fetch("/api/users/" + id, { method: "DELETE", headers: authHeaders() })
                .then(function (r) {
                    if (!r.ok) r.json().then(function (d) { showMessage(d.message || "Error al eliminar.", true); });
                    else { showMessage("Usuario rechazado y eliminado.", false); loadPending(); }
                })
                .catch(function () { showMessage("Error de conexión.", true); });
        }

        document.getElementById("pending-tbody").addEventListener("click", function (e) {
            var btn = e.target.closest("button");
            if (!btn) return;
            var id = parseInt(btn.getAttribute("data-user-id"));
            if (btn.getAttribute("data-action") === "approve") approveUser(id);
            if (btn.getAttribute("data-action") === "reject")  rejectUser(id, btn.getAttribute("data-user-name"));
        });

        document.getElementById("btn-refresh-pending").addEventListener("click", loadPending);

        function showMessage(text, isError) {
            var el = document.getElementById("global-message");
            el.hidden = false; el.textContent = text;
            el.className = isError ? "error" : "success";
            clearTimeout(showMessage._t);
            showMessage._t = setTimeout(function () { el.hidden = true; }, 4000);
        }

        loadPending();
    }());
</script>
</body>
</html>
