<%--
    Pagina de administracion de cadenas.

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
    <title>Bancosol | Cadenas</title>
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

<main class="page-wrapper" aria-label="Chains management page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">← Volver al panel</a>
        <h1>Cadenas de supermercados</h1>
        <p>Gestiona las cadenas participantes en las campañas de Bancosol.</p>
    </div>

    <div class="card">
        <div class="card-header">
            <h2>Listado de cadenas</h2>
            <div class="card-actions">
                <button id="btn-export-chains" class="btn btn-secondary">Exportar datos</button>
                <button class="btn btn-primary" id="btn-nueva">+ Nueva cadena</button>
            </div>
        </div>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Código</th>
                        <th>Participación</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody id="chains-tbody">
                    <tr><td colspan="5" class="table-empty">Cargando cadenas...</td></tr>
                </tbody>
            </table>
        </div>
    </div>
</main>

<div class="modal-backdrop" id="modal-backdrop">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <h3 id="modal-title">Nueva cadena</h3>

        <div class="form-group">
            <label for="input-nombre">Nombre <span class="required-asterisk">*</span></label>
            <input type="text" id="input-nombre" placeholder="Ej: Mercadona" maxlength="255">
        </div>
        <div class="form-group">
            <label for="input-codigo">Código <span class="required-asterisk">*</span></label>
            <input type="text" id="input-codigo" placeholder="Ej: MERC" maxlength="50">
            <p class="field-help">Solo letras, números, guiones y guiones bajos.</p>
        </div>
        <div class="checkbox-group">
            <input type="checkbox" id="input-participacion">
            <label for="input-participacion">Participa en campañas activas</label>
        </div>

        <p class="form-message" id="modal-error"></p>

        <div class="modal-footer">
            <button class="btn-cancel" id="btn-cancelar">Cancelar</button>
            <button class="btn btn-primary" id="btn-guardar">Guardar</button>
        </div>
    </div>
</div>

<div class="toast-container" id="toast-container"></div>

<script>
    (function () {
        var token = '<%= token == null ? "" : token %>';

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

        function renderTable(chains) {
            var tbody = document.getElementById("chains-tbody");
            tbody.innerHTML = "";
            if (!chains.length) {
                tbody.innerHTML = '<tr><td colspan="5" class="table-empty">No hay cadenas registradas.</td></tr>';
                return;
            }
            chains.forEach(function (c) {
                var tr = document.createElement("tr");

                var td1 = document.createElement("td"); td1.textContent = c.id; tr.appendChild(td1);

                var td2 = document.createElement("td");
                var strong = document.createElement("strong"); strong.textContent = escHtml(c.name);
                td2.appendChild(strong); tr.appendChild(td2);

                var td3 = document.createElement("td");
                var code = document.createElement("code"); code.className = "inline-code"; code.textContent = escHtml(c.code);
                td3.appendChild(code); tr.appendChild(td3);

                var td4 = document.createElement("td");
                var badge = document.createElement("span");
                if (c.participation) {
                    badge.className = "badge badge-yes"; badge.textContent = "\u2713 S\u00ed";
                } else {
                    badge.className = "badge badge-no"; badge.textContent = "\u2014 No";
                }
                td4.appendChild(badge); tr.appendChild(td4);

                var td5 = document.createElement("td");
                var div = document.createElement("div"); div.className = "td-actions";

                var btnEdit = document.createElement("button");
                btnEdit.className = "btn btn-edit btn-sm";
                btnEdit.setAttribute("data-action", "edit");
                btnEdit.setAttribute("data-chain-id", c.id);
                btnEdit.textContent = "Editar";
                div.appendChild(btnEdit);

                var btnDelete = document.createElement("button");
                btnDelete.className = "btn btn-danger btn-sm";
                btnDelete.setAttribute("data-action", "delete");
                btnDelete.setAttribute("data-chain-id", c.id);
                btnDelete.setAttribute("data-chain-name", escHtml(c.name));
                btnDelete.textContent = "Eliminar";
                div.appendChild(btnDelete);

                td5.appendChild(div); tr.appendChild(td5);
                tbody.appendChild(tr);
            });
        }

        function loadChains() {
            fetch("/api/chains", { headers: authHeaders() })
                .then(function (r) {
                    if (r.status === 401 || r.status === 403) { return null; }
                    if (!r.ok) throw new Error();
                    return r.json();
                })
                .then(function (d) { if (d) renderTable(d); })
                .catch(function () {
                    document.getElementById("chains-tbody").innerHTML =
                        '<tr><td colspan="5" class="table-empty">No se puede conectar con el servidor.</td></tr>';
                });
        }

        var editingId = null;

        function openModal(titulo) {
            document.getElementById("modal-title").textContent = titulo;
            document.getElementById("modal-error").textContent = "";
            document.getElementById("modal-backdrop").classList.add("open");
            document.getElementById("input-nombre").focus();
        }

        function closeModal() {
            document.getElementById("modal-backdrop").classList.remove("open");
            document.getElementById("input-nombre").value = "";
            document.getElementById("input-codigo").value = "";
            document.getElementById("input-participacion").checked = false;
            document.getElementById("modal-error").textContent = "";
            editingId = null;
        }

        function openEdit(id) {
            fetch("/api/chains/" + id, { headers: authHeaders() })
                .then(function (r) { if (!r.ok) throw new Error(); return r.json(); })
                .then(function (c) {
                    editingId = c.id;
                    document.getElementById("input-nombre").value = c.name;
                    document.getElementById("input-codigo").value = c.code;
                    document.getElementById("input-participacion").checked = !!c.participation;
                    openModal("Editar cadena");
                })
                .catch(function () { showToast("Error al cargar la cadena.", "error"); });
        }

        document.getElementById("chains-tbody").addEventListener("click", function (e) {
            var btn = e.target.closest("button");
            if (!btn) return;
            var id = parseInt(btn.getAttribute("data-chain-id"));
            if (btn.getAttribute("data-action") === "edit")   openEdit(id);
            if (btn.getAttribute("data-action") === "delete") deleteChain(id, btn.getAttribute("data-chain-name"));
        });

        document.getElementById("btn-nueva").addEventListener("click", function () { editingId = null; openModal("Nueva cadena"); });
        document.getElementById("btn-cancelar").addEventListener("click", closeModal);
        document.getElementById("modal-backdrop").addEventListener("click", function (e) {
            if (e.target === document.getElementById("modal-backdrop")) closeModal();
        });
        document.getElementById("btn-export-chains").addEventListener("click", function () {
            fetch("/api/export/chains", { headers: authHeaders() })
                .then(function (r) { return r.blob(); })
                .then(function (blob) {
                    var url = URL.createObjectURL(blob);
                    var a = document.createElement("a");
                    a.href = url;
                    a.download = "chains_export.xlsx";
                    a.click();
                    URL.revokeObjectURL(url);
                });
        });

        document.getElementById("btn-guardar").addEventListener("click", function () {
            var nombre = document.getElementById("input-nombre").value.trim();
            var codigo = document.getElementById("input-codigo").value.trim();
            var part   = document.getElementById("input-participacion").checked;
            var errEl  = document.getElementById("modal-error");

            if (!nombre) { errEl.textContent = "El nombre es obligatorio."; return; }
            if (!codigo) { errEl.textContent = "El código es obligatorio."; return; }
            if (!/^[A-Za-z0-9_\-]+$/.test(codigo)) { errEl.textContent = "El código solo puede contener letras, números, guiones y guiones bajos."; return; }
            if (nombre.length > 255) { errEl.textContent = "El nombre no puede superar 255 caracteres."; return; }
            if (codigo.length > 50)  { errEl.textContent = "El código no puede superar 50 caracteres."; return; }

            var url = editingId ? "/api/chains/" + editingId : "/api/chains";
            fetch(url, { method: editingId ? "PUT" : "POST", headers: authHeaders(), body: JSON.stringify({ name: nombre, code: codigo, participation: part }) })
                .then(function (r) { return r.json().then(function (d) { return { ok: r.ok, data: d }; }); })
                .then(function (r) {
                    if (!r.ok) { errEl.textContent = r.data.message || "Error al guardar."; return; }
                    closeModal(); showToast(editingId ? "Cadena actualizada." : "Cadena creada."); loadChains();
                })
                .catch(function () { errEl.textContent = "Error de conexión."; });
        });

        function deleteChain(id, nombre) {
            if (!confirm("¿Eliminar la cadena \"" + nombre + "\"?\nEsta acción no se puede deshacer.")) return;
            fetch("/api/chains/" + id, { method: "DELETE", headers: authHeaders() })
                .then(function (r) {
                    if (!r.ok) r.json().then(function (d) { showToast(d.message || "Error al eliminar.", "error"); });
                    else { showToast("Cadena eliminada."); loadChains(); }
                })
                .catch(function () { showToast("Error de conexión.", "error"); });
        }

        loadChains();
    }());
</script>
</body>
</html>
