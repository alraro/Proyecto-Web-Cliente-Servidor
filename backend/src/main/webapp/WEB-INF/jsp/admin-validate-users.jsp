<%--
    Pagina de validacion de registros de usuarios.

    Autores:
    - Alejandra Ortiz: 80%
    - IA Generativa: 20%
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String token = (String) session.getAttribute("token");
    String role = (String) session.getAttribute("role");

    if (token == null || !"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Validar registros</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
</head>
<body>

<header class="topbar" aria-label="Top navigation">
    <a class="brand" href="/index" aria-label="Bancosol home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Admin" : nombre %></span>
        </div>
        <button class="btn-edit" id="btn-edit">Editar perfil 🖉</button>
        <button class="btn-logout" id="btn-logout">Cerrar sesión ×</button>
    </div>
</header>

<main class="page-wrapper" aria-label="Validate users page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">← Volver al panel</a>
        <h1>Validar registros</h1>
        <p>Revisa y aprueba o rechaza las solicitudes de nuevos usuarios.</p>
    </div>

    <div class="card mb-lg">
        <div class="card-header">
            <h2>Pendientes de aprobación <span id="badge-pending" class="badge badge-no hidden"></span></h2>
            <div class="card-actions">
                <button type="button" id="btn-refresh-pending" class="btn btn-edit btn-sm">↻ Actualizar</button>
            </div>
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
    </div>
</main>

<div class="toast-container" id="toast-container"></div>

<script>
    (function () {
        var token = '<%= token == null ? "" : token %>';

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

        function escHtml(v) {
            return String(v == null ? "" : v)
                .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
        }

        function showToast(msg, type) {
            var container = document.getElementById("toast-container");
            var toast = document.createElement("div");
            toast.className = "toast " + (type === "error" ? "toast-error" : "toast-success");
            toast.textContent = msg;
            container.appendChild(toast);
            setTimeout(function () { toast.remove(); }, 3500);
        }

        function loadPending() {
            var tbody = document.getElementById("pending-tbody");
            tbody.innerHTML = '<tr><td colspan="6" class="table-empty">Cargando...</td></tr>';

            fetch("/api/users/pending", { headers: authHeaders() })
                .then(function (r) {
                    if (r.status === 401 || r.status === 403) { window.location.href = "/login"; return null; }
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

                        var td1 = document.createElement("td"); td1.textContent = u.id; tr.appendChild(td1);

                        var td2 = document.createElement("td");
                        var strong = document.createElement("strong"); strong.textContent = escHtml(u.name);
                        td2.appendChild(strong); tr.appendChild(td2);

                        var td3 = document.createElement("td"); td3.textContent = escHtml(u.email || "\u2014"); tr.appendChild(td3);
                        var td4 = document.createElement("td"); td4.textContent = escHtml(u.phone || "\u2014"); tr.appendChild(td4);

                        var td5 = document.createElement("td");
                        var select = document.createElement("select");
                        select.id = "role-" + u.id;

                        var roles = [
                            { value: "",                   label: "Seleccionar rol..."    },
                            { value: "ADMINISTRADOR",      label: "Administrador"         },
                            { value: "COORDINADOR",        label: "Coordinador"           },
                            { value: "CAPITAN",            label: "Capit\u00e1n"          },
                            { value: "COLABORADOR",        label: "Colaborador"           },
                            { value: "RESPONSABLE_TIENDA", label: "Responsable de Tienda" }
                        ];
                        roles.forEach(function (r) {
                            var opt = document.createElement("option");
                            opt.value = r.value; opt.textContent = r.label;
                            select.appendChild(opt);
                        });
                        td5.appendChild(select); tr.appendChild(td5);

                        var td6 = document.createElement("td");
                        var div = document.createElement("div"); div.className = "td-actions";

                        var btnApprove = document.createElement("button");
                        btnApprove.className = "btn btn-primary btn-sm";
                        btnApprove.setAttribute("data-action", "approve");
                        btnApprove.setAttribute("data-user-id", u.id);
                        btnApprove.textContent = "\u2713 Aprobar";
                        div.appendChild(btnApprove);

                        var btnReject = document.createElement("button");
                        btnReject.className = "btn btn-danger btn-sm";
                        btnReject.setAttribute("data-action", "reject");
                        btnReject.setAttribute("data-user-id", u.id);
                        btnReject.setAttribute("data-user-name", escHtml(u.name));
                        btnReject.textContent = "\u2717 Rechazar";
                        div.appendChild(btnReject);

                        td6.appendChild(div); tr.appendChild(td6);
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
            if (!roleVal) { showToast("Selecciona un rol antes de aprobar.", "error"); return; }
            fetch("/api/users/" + id + "/role", { method: "POST", headers: authHeaders(), body: JSON.stringify({ role: roleVal }) })
                .then(function (r) { return r.json().then(function (d) { return { ok: r.ok, data: d }; }); })
                .then(function (r) {
                    if (!r.ok) { showToast(r.data.message || "Error al asignar rol.", "error"); return; }
                    showToast("Usuario aprobado como " + roleVal + "."); loadPending();
                })
                .catch(function () { showToast("Error de conexión.", "error"); });
        }

        function rejectUser(id, name) {
            if (!confirm("¿Rechazar y eliminar la cuenta de \"" + name + "\"?\nEsta acción no se puede deshacer.")) return;
            fetch("/api/users/" + id, { method: "DELETE", headers: authHeaders() })
                .then(function (r) {
                    if (!r.ok) r.json().then(function (d) { showToast(d.message || "Error al eliminar.", "error"); });
                    else { showToast("Usuario rechazado y eliminado."); loadPending(); }
                })
                .catch(function () { showToast("Error de conexión.", "error"); });
        }

        document.getElementById("pending-tbody").addEventListener("click", function (e) {
            var btn = e.target.closest("button");
            if (!btn) return;
            var id = parseInt(btn.getAttribute("data-user-id"));
            if (btn.getAttribute("data-action") === "approve") approveUser(id);
            if (btn.getAttribute("data-action") === "reject")  rejectUser(id, btn.getAttribute("data-user-name"));
        });

        document.getElementById("btn-refresh-pending").addEventListener("click", loadPending);

        loadPending();
    }());
</script>
</body>
</html>
