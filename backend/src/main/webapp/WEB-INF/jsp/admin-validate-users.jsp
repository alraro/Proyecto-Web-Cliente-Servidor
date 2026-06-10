<%--
    Página de gestión de usuarios (listado completo, cambio de rol, eliminar).
    Autores: [vuestros nombres]
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String token  = (String) session.getAttribute("token");
    String role   = (String) session.getAttribute("role");

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
    <title>Bancosol | Usuarios</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
</head>
<body>

<header class="topbar">
    <a class="brand" href="/">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-right">
        <div class="user-badge">
            <span class="dot"></span>
            <span id="user-name"><%= nombre == null ? "Admin" : nombre %></span>
        </div>
        <a href="/edit" class="btn-edit" id="btn-edit">Editar perfil 🖉</a>
        <a href="/logout" class="btn-logout" id="btn-logout">Cerrar sesión ×</a>
    </div>
</header>

<main class="page-wrapper" aria-label="Pending users page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">← Volver al panel</a>
        <h1>Validar registros</h1>
        <p>Revisa y aprueba o rechaza las solicitudes de nuevos usuarios.</p>
    </div>

    <div class="card">
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
    var usersCache = [];

    function authHeaders() {
        return { "Content-Type": "application/json", "Authorization": "Bearer " + token };
    }

    function escHtml(v) {
        return String(v == null ? "" : v)
            .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    function showToast(msg, type) {
        var c = document.querySelector("#toast-container");
        var t = document.createElement("div");
        t.className = "toast " + (type === "error" ? "toast-error" : "toast-success");
        t.textContent = msg;
        c.appendChild(t);
        setTimeout(function () { t.remove(); }, 3500);
    }

    function createRoleSelect(userId) {
        var select = document.createElement("select");
        select.id = "role-" + userId;

        var optDefault = document.createElement("option");
        optDefault.value = "";
        optDefault.textContent = "Seleccionar rol...";
        select.appendChild(optDefault);

        ["ADMINISTRADOR", "COORDINADOR", "CAPITAN", "COLABORADOR", "RESPONSABLE_TIENDA"].forEach(function (role) {
            var opt = document.createElement("option");
            opt.value = role;
            opt.textContent = role;
            select.appendChild(opt);
        });

        return select;
    }

    function renderRow(u) {
        var tr = document.createElement("tr");

        var td1 = document.createElement("td");
        td1.textContent = u.idUser;
        tr.appendChild(td1);

        var td2 = document.createElement("td");
        var strong = document.createElement("strong");
        strong.textContent = escHtml(u.name);
        td2.appendChild(strong);
        tr.appendChild(td2);

        var td3 = document.createElement("td");
        td3.textContent = escHtml(u.email || "—");
        tr.appendChild(td3);

        var td4 = document.createElement("td");
        td4.textContent = escHtml(u.phone || "—");
        tr.appendChild(td4);

        var td5 = document.createElement("td");
        td5.appendChild(createRoleSelect(u.idUser));
        tr.appendChild(td5);

        var td6 = document.createElement("td");
        var div = document.createElement("div");
        div.className = "td-actions";

        var btnApprove = document.createElement("button");
        btnApprove.className = "btn btn-primary btn-sm";
        btnApprove.setAttribute("data-action", "approve");
        btnApprove.setAttribute("data-user-id", u.idUser);
        btnApprove.textContent = "✓ Aprobar";
        div.appendChild(btnApprove);

        var btnReject = document.createElement("button");
        btnReject.className = "btn btn-danger btn-sm";
        btnReject.setAttribute("data-action", "reject");
        btnReject.setAttribute("data-user-id", u.idUser);
        btnReject.setAttribute("data-user-name", escHtml(u.name));
        btnReject.textContent = "✗ Rechazar";
        div.appendChild(btnReject);

        td6.appendChild(div);
        tr.appendChild(td6);
        return tr;
    }

    function loadPending() {
        var tbody = document.querySelector("#pending-tbody");
        tbody.innerHTML = '<tr><td colspan="6" class="table-empty">Cargando...</td></tr>';

        fetch("/api/users/pending", { headers: authHeaders() })
            .then(function (r) {
                if (r.status === 401 || r.status === 403) { return null; }
                if (!r.ok) throw new Error();
                return r.json();
            })
            .then(function (data) {
                var badge = document.querySelector("#badge-pending");
                if (!data) return;
                usersCache = data;
                if (data.length > 0) {
                    badge.textContent = data.length;
                    badge.classList.remove("hidden");
                } else {
                    badge.textContent = "";
                    badge.classList.add("hidden");
                }
                tbody.innerHTML = "";
                if (!data.length) {
                    tbody.innerHTML = '<tr><td colspan="6" class="table-empty">No hay usuarios pendientes de aprobación.</td></tr>';
                    return;
                }
                data.forEach(function (u) { tbody.appendChild(renderRow(u)); });
            })
            .catch(function () {
                document.querySelector("#pending-tbody").innerHTML =
                    '<tr><td colspan="6" class="table-empty">Error al conectar con el servidor.</td></tr>';
            });
    }

    function approveUser(userId) {
        var role = document.querySelector("#role-" + userId).value;
        if (!role) {
            showToast("Selecciona un rol antes de aprobar.", "error");
            return;
        }

        fetch("/api/users/" + userId + "/role", {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({ role: role })
        })
            .then(function (r) {
                return r.json().then(function (d) {
                    return { ok: r.ok, data: d };
                });
            })
            .then(function (result) {
                if (!result.ok) {
                    showToast(result.data.message || "Error al asignar rol.", "error");
                    return;
                }
                showToast("Usuario aprobado correctamente.");
                loadPending();
            })
            .catch(function () {
                showToast("Error de conexión.", "error");
            });
    }

    function rejectUser(userId, userName) {
        if (!confirm("¿Rechazar y eliminar la cuenta de \"" + userName + "\"? Esta acción no se puede deshacer.")) return;

        fetch("/api/users/" + userId, {
            method: "DELETE",
            headers: authHeaders()
        })
            .then(function (r) {
                return r.json().then(function (d) {
                    return { ok: r.ok, data: d };
                });
            })
            .then(function (result) {
                if (!result.ok) {
                    showToast(result.data.message || "Error al eliminar.", "error");
                    return;
                }
                showToast("Usuario rechazado y eliminado.");
                loadPending();
            })
            .catch(function () {
                showToast("Error de conexión.", "error");
            });
    }

    document.querySelector("#btn-refresh-pending").addEventListener("click", loadPending);
    document.querySelector("#pending-tbody").addEventListener("click", function (e) {
        var btn = e.target.closest("button");
        if (!btn) return;

        var action = btn.getAttribute("data-action");
        var userId = btn.getAttribute("data-user-id");
        if (action === "approve") {
            approveUser(userId);
        } else if (action === "reject") {
            rejectUser(userId, btn.getAttribute("data-user-name"));
        }
    });

    loadPending();
}());
</script>
</body>
</html>