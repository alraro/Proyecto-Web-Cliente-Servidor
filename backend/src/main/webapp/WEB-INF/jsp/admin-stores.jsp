<%--
    Pagina de administracion de tiendas.

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
    <title>Bancosol | Tiendas</title>
    <link rel="icon" type="image/png" href="/assets/Bancosol.png">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <link rel="stylesheet" href="/css/admin-stores.css">
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

<main class="page-wrapper" aria-label="Stores management page">
    <div class="page-header">
        <a href="/admin" class="back-link-inline">← Volver al panel</a>
        <h1>Tiendas</h1>
        <p>Gestión de tiendas asociadas a cadenas de supermercados.</p>
    </div>

    <div class="card">
        <div class="card-header">
            <h2>Listado de tiendas</h2>
            <div class="card-actions">
                <button id="btn-export-stores" class="btn btn-secondary">Exportar datos</button>
                <button class="btn btn-primary" id="btn-nueva-tienda">+ Nueva tienda</button>
            </div>
        </div>

        <div class="filters-bar">
            <select id="filter-zone"><option value="">Todas las zonas</option></select>
            <select id="filter-locality"><option value="">Todas las localidades</option></select>
            <select id="filter-chain"><option value="">Todas las cadenas</option></select>
            <button type="button" id="btn-apply-filters">Filtrar</button>
            <button type="button" class="btn-clear" id="btn-clear-filters">Limpiar</button>
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
    </div>
</main>

<div class="modal-backdrop" id="modal-backdrop">
    <div class="modal modal-store" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <h3 id="modal-title">Nueva tienda</h3>

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
            <button class="btn-cancel" id="btn-modal-cancel">Cancelar</button>
            <button class="btn btn-primary" id="btn-modal-save">Guardar</button>
        </div>
    </div>
</div>

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

                var td1 = document.createElement("td"); td1.textContent = s.id; tr.appendChild(td1);

                var td2 = document.createElement("td");
                var strong = document.createElement("strong"); strong.textContent = escHtml(s.name);
                td2.appendChild(strong); tr.appendChild(td2);

                var td3 = document.createElement("td"); td3.textContent = escHtml(s.address || "\u2014"); tr.appendChild(td3);
                var td4 = document.createElement("td"); td4.textContent = escHtml(s.locality || "\u2014"); tr.appendChild(td4);
                var td5 = document.createElement("td"); td5.textContent = escHtml(s.postalCode || "\u2014"); tr.appendChild(td5);
                var td6 = document.createElement("td"); td6.textContent = escHtml(s.zone || "\u2014"); tr.appendChild(td6);
                var td7 = document.createElement("td"); td7.textContent = escHtml(s.chainName || "\u2014"); tr.appendChild(td7);

                var td8 = document.createElement("td");
                var div = document.createElement("div"); div.className = "td-actions";

                var btnEdit = document.createElement("button");
                btnEdit.className = "btn btn-edit btn-sm";
                btnEdit.setAttribute("data-action", "edit");
                btnEdit.setAttribute("data-store-id", s.id);
                btnEdit.textContent = "Editar";
                div.appendChild(btnEdit);

                var btnDelete = document.createElement("button");
                btnDelete.className = "btn btn-danger btn-sm";
                btnDelete.setAttribute("data-action", "delete");
                btnDelete.setAttribute("data-store-id", s.id);
                btnDelete.setAttribute("data-store-name", escHtml(s.name));
                btnDelete.textContent = "Eliminar";
                div.appendChild(btnDelete);

                td8.appendChild(div); tr.appendChild(td8);
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
                    if (r.status === 401 || r.status === 403) { window.location.href = "/login"; return null; }
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
        document.getElementById("btn-export-stores").addEventListener("click", function () {
            window.location.href = "/api/export/stores";
        });

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
            document.getElementById("modal-backdrop").classList.add("open");
            document.getElementById("input-nombre").focus();
        }

        function closeModal() {
            document.getElementById("modal-backdrop").classList.remove("open");
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
                .catch(function () { showToast("Error al cargar la tienda.", "error"); });
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
                    closeModal(); showToast(editingId ? "Tienda actualizada." : "Tienda creada."); loadStores(currentPage);
                })
                .catch(function () { errEl.textContent = "Error de conexión."; });
        });

        function deleteStore(id, nombre) {
            if (!confirm("¿Eliminar la tienda \"" + nombre + "\"?\nEsta acción no se puede deshacer.")) return;
            fetch("/api/stores/" + id, { method: "DELETE", headers: authHeaders() })
                .then(function (r) {
                    if (!r.ok) r.json().then(function (d) { showToast(d.message || "Error al eliminar.", "error"); });
                    else { showToast("Tienda eliminada."); loadStores(currentPage); }
                })
                .catch(function () { showToast("Error de conexión.", "error"); });
        }

        loadAuxData().then(function () { loadStores(0); });
    }());
</script>
</body>
</html>
