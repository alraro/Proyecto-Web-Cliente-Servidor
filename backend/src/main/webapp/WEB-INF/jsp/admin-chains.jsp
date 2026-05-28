<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Cadenas</title>
    <link rel="stylesheet" href="/css/administrador.css">
    <link rel="stylesheet" href="/css/admin-chains-stores.css">
</head>
<body>

<header class="topbar">
    <a class="brand" href="/index" aria-label="Bancosol admin home">
        <img src="/assets/LOGO_BANCOSOL.png" alt="Bancosol logo" class="logo">
    </a>
    <div class="topbar-actions">
        <span id="user-name"><%= nombre == null ? "Admin" : nombre %></span>
        <a href="/edit" class="edit-link">Editar perfil</a>
        <a href="/login" class="logout-link">Cerrar sesión</a>    
    </div>
</header>

<main class="admin-page" aria-label="Chains management page">
    <section class="page-header">
        <a href="/admin" class="back-link">&larr; Volver al menú</a>
        <div class="page-header-row">
            <div>
                <h1>Cadenas de supermercados</h1>
                <p>Gestiona las cadenas participantes en las campañas de Bancosol.</p>
            </div>
            <button type="button" id="btn-nueva" class="btn-primary">+ Nueva cadena</button>
        </div>
    </section>

    <div id="global-message" hidden></div>

    <section class="card" aria-label="Listado de cadenas">
        <h2>Listado de cadenas</h2>
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
    </section>
</main>

<div id="modal-backdrop" class="modal-overlay" aria-hidden="true">
    <div class="modal-card" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <h2 id="modal-title">Nueva cadena</h2>

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
            <button type="button" class="btn-cancel" id="btn-cancelar">Cancelar</button>
            <button type="button" class="btn-primary" id="btn-guardar">Guardar</button>
        </div>
    </div>
</div>

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

        function renderTable(chains) {
            var tbody = document.getElementById("chains-tbody");
            tbody.innerHTML = "";
            if (!chains.length) {
                tbody.innerHTML = '<tr><td colspan="5" class="table-empty">No hay cadenas registradas.</td></tr>';
                return;
            }
            chains.forEach(function (c) {
                var tr = document.createElement("tr");
                tr.innerHTML =
                    "<td>" + c.id + "</td>" +
                    "<td><strong>" + escHtml(c.name) + "</strong></td>" +
                    "<td><code class='inline-code'>" + escHtml(c.code) + "</code></td>" +
                    "<td>" + (c.participation
                        ? '<span class="badge-yes">\u2713 S\u00ed</span>'
                        : '<span class="badge-no">\u2014 No</span>') + "</td>" +
                    "<td><div class='td-actions'>" +
                        "<button class='btn btn-edit btn-sm' data-action='edit'   data-chain-id='" + c.id + "'>Editar</button>" +
                        "<button class='btn-danger btn-sm'  data-action='delete' data-chain-id='" + c.id + "' data-chain-name='" + escHtml(c.name) + "'>Eliminar</button>" +
                    "</div></td>";
                tbody.appendChild(tr);
            });
        }

        function loadChains() {
            fetch("/api/chains", { headers: authHeaders() })
                .then(function (r) {
                    if (r.status === 401 || r.status === 403) { localStorage.clear(); window.location.href = "/login"; return null; }
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
            var bd = document.getElementById("modal-backdrop");
            bd.style.display = "flex"; bd.setAttribute("aria-hidden", "false");
            document.getElementById("input-nombre").focus();
        }

        function closeModal() {
            var bd = document.getElementById("modal-backdrop");
            bd.style.display = "none"; bd.setAttribute("aria-hidden", "true");
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
                .catch(function () { showMessage("Error al cargar la cadena.", true); });
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
                    closeModal();
                    showMessage(editingId ? "Cadena actualizada." : "Cadena creada.", false);
                    loadChains();
                })
                .catch(function () { errEl.textContent = "Error de conexión."; });
        });

        function deleteChain(id, nombre) {
            if (!confirm("¿Eliminar la cadena \"" + nombre + "\"?\nEsta acción no se puede deshacer.")) return;
            fetch("/api/chains/" + id, { method: "DELETE", headers: authHeaders() })
                .then(function (r) {
                    if (!r.ok) r.json().then(function (d) { showMessage(d.message || "Error al eliminar.", true); });
                    else { showMessage("Cadena eliminada.", false); loadChains(); }
                })
                .catch(function () { showMessage("Error de conexión.", true); });
        }

        function showMessage(text, isError) {
            var el = document.getElementById("global-message");
            el.hidden = false; el.textContent = text;
            el.className = isError ? "error" : "success";
            clearTimeout(showMessage._t);
            showMessage._t = setTimeout(function () { el.hidden = true; }, 4000);
        }

        loadChains();
    }());
</script>
</body>
</html>
