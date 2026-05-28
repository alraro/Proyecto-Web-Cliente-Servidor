<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
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
    <title>Bancosol | Tiendas</title>
    <link rel="stylesheet" href="/css/administrador.css">
    <link rel="stylesheet" href="/css/admin-chains-stores.css">
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

<main class="admin-page" aria-label="Stores management page">
    <section class="page-header">
        <a href="/admin" class="back-link">&larr; Volver al menú</a>
        <div class="page-header-row">
            <div>
                <h1>Tiendas</h1>
                <p>Gestión de tiendas asociadas a cadenas de supermercados.</p>
            </div>
            <button type="button" id="btn-nueva-tienda" class="btn-primary">+ Nueva tienda</button>
        </div>
    </section>

    <div id="global-message" hidden></div>

    <section class="card" aria-label="Listado de tiendas">
        <div class="filters-bar">
            <select id="filter-zone"><option value="">Todas las zonas</option></select>
            <select id="filter-locality"><option value="">Todas las localidades</option></select>
            <select id="filter-chain"><option value="">Todas las cadenas</option></select>
            <button type="button" id="btn-apply-filters" class="btn-filter">Filtrar</button>
            <button type="button" id="btn-clear-filters" class="btn-clear">Limpiar</button>
        </div>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Domicilio</th>
                        <th>Localidad</th>
                        <th>CP</th>
                        <th>Zona</th>
                        <th>Cadena</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody id="stores-tbody">
                    <tr><td colspan="8" class="table-empty">Cargando tiendas...</td></tr>
                </tbody>
            </table>
        </div>

        <div class="pagination">
            <button type="button" id="btn-prev-page">← Anterior</button>
            <span class="pagination-info">
                Página <span id="current-page">1</span> de <span id="total-pages">1</span>
            </span>
            <button type="button" id="btn-next-page">Siguiente →</button>
            <select class="pagination-select" id="page-size-select">
                <option value="20">20 por página</option>
                <option value="50">50 por página</option>
                <option value="100">100 por página</option>
            </select>
        </div>
    </section>
</main>

<div id="modal-backdrop" class="modal-overlay" aria-hidden="true">
    <div class="modal-card" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <h2 id="modal-title">Nueva tienda</h2>

        <div class="form-group">
            <label for="input-nombre">Nombre <span class="required-asterisk">*</span></label>
            <input type="text" id="input-nombre" placeholder="Nombre de la tienda" maxlength="255">
        </div>
        <div class="form-group">
            <label for="input-domicilio">Domicilio</label>
            <input type="text" id="input-domicilio" placeholder="Calle, número..." maxlength="500">
        </div>
        <div class="form-group">
            <label for="input-cp">Código postal</label>
            <input type="text" id="input-cp" placeholder="Ej: 28001" maxlength="5">
        </div>
        <div class="form-group">
            <label for="input-chain">Cadena</label>
            <select id="input-chain">
                <option value="">Sin cadena asignada</option>
            </select>
        </div>

        <p class="form-message" id="modal-error"></p>

        <div class="modal-footer">
            <button type="button" class="btn-cancel" id="btn-modal-cancel">Cancelar</button>
            <button type="button" class="btn-primary" id="btn-modal-save">Guardar</button>
        </div>
    </div>
</div>

<script>
    (function () {
        var token = '<%= token == null ? "" : token %>';

        document.getElementById("user-name").textContent = '<%= nombre == null ? "Admin" : nombre %>';
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

        var allChains = [], allLocalities = [], allZones = [];

        function loadAuxData() {
            return Promise.all([
                fetch("/api/chains",     { headers: authHeaders() }).then(function (r) { return r.ok ? r.json() : []; }),
                fetch("/api/localities", { headers: authHeaders() }).then(function (r) { return r.ok ? r.json() : []; }),
                fetch("/api/zones",      { headers: authHeaders() }).then(function (r) { return r.ok ? r.json() : []; })
            ]).then(function (res) {
                allChains = res[0]; allLocalities = res[1]; allZones = res[2];

                var fz = document.getElementById("filter-zone");
                allZones.forEach(function (z) {
                    var o = document.createElement("option"); o.value = z.id; o.textContent = z.name; fz.appendChild(o);
                });
                populateLocalities("");

                var fc = document.getElementById("filter-chain");
                var fch = document.getElementById("input-chain");
                allChains.forEach(function (c) {
                    [fc, fch].forEach(function (sel) {
                        var o = document.createElement("option"); o.value = c.id; o.textContent = c.name; sel.appendChild(o);
                    });
                });
            });
        }

        document.getElementById("filter-zone").addEventListener("change", function () {
            document.getElementById("filter-locality").value = "";
            populateLocalities(this.value);
        });

        function populateLocalities(zoneId) {
            var sel = document.getElementById("filter-locality");
            var prev = sel.value;
            sel.innerHTML = '<option value="">Todas las localidades</option>';
            var lista = zoneId ? allLocalities.filter(function (l) { return String(l.zoneId) === String(zoneId); }) : allLocalities;
            lista.forEach(function (l) {
                var o = document.createElement("option"); o.value = l.id; o.textContent = l.name; sel.appendChild(o);
            });
            if (prev && lista.some(function (l) { return String(l.id) === String(prev); })) sel.value = prev;
        }

        var currentPage = 0, pageSize = 20, totalPages = 1;

        function renderTable(stores) {
            var tbody = document.getElementById("stores-tbody");
            tbody.innerHTML = "";
            if (!stores.length) {
                tbody.innerHTML = '<tr><td colspan="8" class="table-empty">No hay tiendas que coincidan con los filtros.</td></tr>';
                return;
            }
            stores.forEach(function (s) {
                var tr = document.createElement("tr");
                tr.innerHTML =
                    "<td>" + s.id + "</td>" +
                    "<td><strong>" + escHtml(s.name) + "</strong></td>" +
                    "<td>" + escHtml(s.address || "\u2014") + "</td>" +
                    "<td>" + escHtml(s.locality || "\u2014") + "</td>" +
                    "<td>" + escHtml(s.postalCode || "\u2014") + "</td>" +
                    "<td>" + escHtml(s.zone || "\u2014") + "</td>" +
                    "<td>" + escHtml(s.chainName || "\u2014") + "</td>" +
                    "<td><div class='td-actions'>" +
                        "<button class='btn btn-edit btn-sm' data-action='edit'   data-store-id='" + s.id + "'>Editar</button>" +
                        "<button class='btn-danger btn-sm'  data-action='delete' data-store-id='" + s.id + "' data-store-name='" + escHtml(s.name) + "'>Eliminar</button>" +
                    "</div></td>";
                tbody.appendChild(tr);
            });
        }

        function loadStores(page) {
            page = page || 0;
            var params = new URLSearchParams();
            var chainId = document.getElementById("filter-chain").value;
            var locId   = document.getElementById("filter-locality").value;
            var zoneId  = document.getElementById("filter-zone").value;
            if (chainId) params.append("chainId", chainId);
            if (locId)   params.append("localityId", locId);
            if (zoneId)  params.append("zoneId", zoneId);
            params.append("page", page); params.append("size", pageSize);

            fetch("/api/stores?" + params, { headers: authHeaders() })
                .then(function (r) {
                    if (r.status === 401 || r.status === 403) { localStorage.clear(); window.location.href = "/login"; return null; }
                    if (!r.ok) throw new Error();
                    return r.json();
                })
                .then(function (d) {
                    if (!d) return;
                    currentPage = page; totalPages = d.totalPages || 1;
                    document.getElementById("current-page").textContent = currentPage + 1;
                    document.getElementById("total-pages").textContent  = totalPages;
                    document.getElementById("btn-prev-page").disabled = currentPage === 0;
                    document.getElementById("btn-next-page").disabled = currentPage >= totalPages - 1;
                    renderTable(d.content || []);
                })
                .catch(function () {
                    document.getElementById("stores-tbody").innerHTML =
                        '<tr><td colspan="8" class="table-empty">No se puede conectar con el servidor.</td></tr>';
                });
        }

        document.getElementById("btn-apply-filters").addEventListener("click", function () { loadStores(0); });
        document.getElementById("btn-clear-filters").addEventListener("click", function () {
            document.getElementById("filter-zone").value = "";
            document.getElementById("filter-locality").value = "";
            document.getElementById("filter-chain").value = "";
            populateLocalities(""); loadStores(0);
        });
        document.getElementById("btn-prev-page").addEventListener("click", function () { if (currentPage > 0) loadStores(currentPage - 1); });
        document.getElementById("btn-next-page").addEventListener("click", function () { if (currentPage < totalPages - 1) loadStores(currentPage + 1); });
        document.getElementById("page-size-select").addEventListener("change", function () { pageSize = parseInt(this.value); loadStores(0); });

        document.getElementById("stores-tbody").addEventListener("click", function (e) {
            var btn = e.target.closest("button");
            if (!btn) return;
            var id = parseInt(btn.getAttribute("data-store-id"));
            if (btn.getAttribute("data-action") === "edit")   openEdit(id);
            if (btn.getAttribute("data-action") === "delete") deleteStore(id, btn.getAttribute("data-store-name"));
        });

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
            ["input-nombre","input-domicilio","input-cp"].forEach(function (id) { document.getElementById(id).value = ""; });
            document.getElementById("input-chain").value = "";
            document.getElementById("modal-error").textContent = "";
            editingId = null;
        }

        function openEdit(id) {
            fetch("/api/stores/" + id, { headers: authHeaders() })
                .then(function (r) { if (!r.ok) throw new Error(); return r.json(); })
                .then(function (s) {
                    editingId = s.id;
                    document.getElementById("input-nombre").value    = s.name || "";
                    document.getElementById("input-domicilio").value = s.address || "";
                    document.getElementById("input-cp").value        = s.postalCode || "";
                    document.getElementById("input-chain").value     = s.chainId || "";
                    openModal("Editar tienda");
                })
                .catch(function () { showMessage("Error al cargar la tienda.", true); });
        }

        document.getElementById("btn-nueva-tienda").addEventListener("click", function () { editingId = null; openModal("Nueva tienda"); });
        document.getElementById("btn-modal-cancel").addEventListener("click", closeModal);
        document.getElementById("modal-backdrop").addEventListener("click", function (e) {
            if (e.target === document.getElementById("modal-backdrop")) closeModal();
        });

        document.getElementById("btn-modal-save").addEventListener("click", function () {
            var nombre  = document.getElementById("input-nombre").value.trim();
            var dom     = document.getElementById("input-domicilio").value.trim();
            var cp      = document.getElementById("input-cp").value.trim();
            var chainId = document.getElementById("input-chain").value;
            var errEl   = document.getElementById("modal-error");

            if (!nombre) { errEl.textContent = "El nombre es obligatorio."; return; }
            if (nombre.length > 255) { errEl.textContent = "El nombre no puede superar 255 caracteres."; return; }
            if (cp && !/^\d{5}$/.test(cp)) { errEl.textContent = "El código postal debe tener exactamente 5 dígitos."; return; }

            var url = editingId ? "/api/stores/" + editingId : "/api/stores";
            fetch(url, { method: editingId ? "PUT" : "POST", headers: authHeaders(),
                body: JSON.stringify({ name: nombre, address: dom || null, postalCode: cp || null, chainId: chainId ? parseInt(chainId) : null }) })
                .then(function (r) { return r.json().then(function (d) { return { ok: r.ok, data: d }; }); })
                .then(function (r) {
                    if (!r.ok) { errEl.textContent = r.data.message || "Error al guardar."; return; }
                    closeModal(); showMessage(editingId ? "Tienda actualizada." : "Tienda creada.", false); loadStores(currentPage);
                })
                .catch(function () { errEl.textContent = "Error de conexión."; });
        });

        function deleteStore(id, nombre) {
            if (!confirm("¿Eliminar la tienda \"" + nombre + "\"?\nEsta acción no se puede deshacer.")) return;
            fetch("/api/stores/" + id, { method: "DELETE", headers: authHeaders() })
                .then(function (r) {
                    if (!r.ok) r.json().then(function (d) { showMessage(d.message || "Error al eliminar.", true); });
                    else { showMessage("Tienda eliminada.", false); loadStores(currentPage); }
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

        loadAuxData().then(function () { loadStores(0); });
    }());
</script>
</body>
</html>
