<!--
-
- Autores:
-	- Hugo Herrero González: 90%
-   - IA Generativa: 5%
-   - Página Internet: 5%
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, es.grupo8.backend.entity.Campaign, es.grupo8.backend.dto.AdminDTO" %>
<%
    String nombre = (String) session.getAttribute("nombre");
    String token = (String) session.getAttribute("token");
    String role = (String) session.getAttribute("role");

    if (!"ADMINISTRADOR".equals(role)) {
        response.sendRedirect("/login");
        return;
    }

    List<Campaign> campaignsList = (List<Campaign>) request.getAttribute("campaignsList");
    Integer selectedCampaignId = (Integer) request.getAttribute("selectedCampaignId");
    
    List<AdminDTO> chainData = (List<AdminDTO>) request.getAttribute("chainData");
    List<AdminDTO> localityData = (List<AdminDTO>) request.getAttribute("localityData");    
    List<AdminDTO> zoneData = (List<AdminDTO>) request.getAttribute("zoneData");

    Long kpiStores = (Long) request.getAttribute("kpiStores");
    Long kpiChains = (Long) request.getAttribute("kpiChains");
    Long kpiZones = (Long) request.getAttribute("kpiZones");
    String kpiStatus = (String) request.getAttribute("kpiStatus");

%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bancosol | Dashboard de Cobertura</title>
    <link rel="stylesheet" href="/css/dashboard.css">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/layout.css">
    <link rel="stylesheet" href="/css/admin.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

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

<main class="dashboard-main">

    <div class="page-header">
        <a href="/admin" class="back-link">← Volver al panel</a>
        <h1>Dashboard de Cobertura</h1>
        <p>Visualiza métricas de cobertura por cadena, localidad y zona geográfica.</p>
    </div>

    <section class="controls">
        <form method="get" action="/admin-dashboard">
            <label for="campaignSelect">Campaña:</label>
            <select id="campaignSelect" name="campaignId" onChange="this.form.submit()">
                <option value="">Selecciona una campaña</option>
                <% if (campaignsList != null) { %>
                    <% for (Campaign campaign : campaignsList) { %>
                        <option value="<%= campaign.getId() %>" <%= (selectedCampaignId != null && selectedCampaignId == campaign.getId()) ? "selected" : ""  %>>
                            <%= campaign.getName() %>
                        </option>
                    <% }} %>
            </select>
        </form>
    </section>    

    
    <% if (selectedCampaignId != null) {%>
    <section class="kpi-row" id="kpiRow">
        <div class="kpi-card">
            <span class="kpi-label">Tiendas en campaña</span>
            <span class="kpi-value" id="kpiStores"><%= kpiStores != null ? kpiStores : "-" %></span>
        </div>

        <div class="kpi-card">
            <span class="kpi-label">Estado</span>
            <span class="kpi-value" id="kpiStatus"><%= kpiStatus != null ? kpiStatus : "-" %></span>
        </div>

        <div class="kpi-card">
            <span class="kpi-label">Cadenas cubiertas</span>
            <span class="kpi-value" id="kpiChains"><%= kpiChains != null ? kpiChains : "-" %></span>
        </div>

        <div class="kpi-card">
            <span class="kpi-label">Zonas cubiertas</span>
            <span class="kpi-value" id="kpiZones"><%= kpiZones != null ? kpiZones : "-" %></span>
        </div>
    </section>



    <section class="charts-grid" id="chartsGrid">

        <div class="chart-card">
            <h3>Cobertura por Cadena</h3>
            <div class="chart-wrap"><canvas id="chainChart"></canvas></div>
        </div>

        <div class="chart-card">
            <h3>Cobertura por Localidad</h3>
            <div class="chart-wrap"><canvas id="localityChart"></canvas></div>
        </div>

        <div class="chart-card chart-card--wide">
            <h3>Cobertura por Zona Geográfica</h3>
            <div class="chart-wrap"><canvas id="zoneChart"></canvas></div>
        </div>
    </section>
    <% } else { %>
        <div id="noSelection" class="no-selection">Selecciona una campaña para ver las métricas de cobertura.</div>
    <% } %>

    <div id="errorMsg" class="error-msg hidden"></div>

</main>
<% if (selectedCampaignId != null && chainData != null && localityData != null && zoneData != null) { %>
<script>
    function renderChart(canvasId, labels, covered, total, pct, axis) {
        new Chart(document.querySelector("#" + canvasId), {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Tiendas en campaña',
                        data: covered,
                        backgroundColor: 'rgba(59,130,246,0.75)',
                        borderColor:     'rgba(59,130,246,1)',
                        borderWidth: 1
                    },
                    {
                        label: 'Total de tiendas',
                        data: total,
                        backgroundColor: 'rgba(209,213,219,0.5)',
                        borderColor:     'rgba(156,163,175,1)',
                        borderWidth: 1
                    }
                ]
            },
            options: {
                indexAxis: axis,
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index', 
                    intersect: false
                },
                plugins: {
                    legend: {
                        position: 'top',
                    },
                    tooltip: {
                        callbacks: {
                            afterBody: (items) => ['Cobertura: ' + pct[items[0].dataIndex] + '%']
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }

    var chainLabels = [<% for (int i=0; i<chainData.size(); i++) {%>
        "<%= chainData.get(i).getLabel() %>"<%= i<chainData.size()-1 ? "," : "" %>
    <%}%>];

    var chainCovered = [<% for (int i=0; i<chainData.size(); i++) {%>
        <%= chainData.get(i).getStoresInCampaign() %><%= i<chainData.size()-1 ? "," : "" %>
    <%}%>];

    var chainTotal =  [<% for (int i=0; i<chainData.size(); i++) {%>
        <%= chainData.get(i).getTotalStores() %><%= i<chainData.size()-1 ? "," : "" %>
    <%}%>];

    var chainPct = [<% for (int i=0; i<chainData.size(); i++) {%>
        <%= chainData.get(i).getCoveragePercentage() %><%= i<chainData.size()-1 ? "," : "" %>
    <%}%>];

    renderChart("chainChart", chainLabels, chainCovered, chainTotal, chainPct, "x");



    var localityLabels = [<% for (int i=0; i<localityData.size(); i++) {%>
        "<%= localityData.get(i).getLabel() %>"<%= i<localityData.size()-1 ? "," : "" %>
    <%}%>];

    var localityCovered = [<% for (int i=0; i<localityData.size(); i++) {%>
        <%= localityData.get(i).getStoresInCampaign() %><%= i<localityData.size()-1 ? "," : "" %>
    <%}%>];

    var localityTotal = [<% for (int i=0; i<localityData.size(); i++) {%>
        <%= localityData.get(i).getTotalStores() %><%= i<localityData.size()-1 ? "," : "" %>
    <%}%>];

    var localityPct = [<% for (int i=0; i<localityData.size(); i++) {%>
        <%= localityData.get(i).getCoveragePercentage() %><%= i<localityData.size()-1 ? "," : "" %>
    <%}%>];

    renderChart("localityChart", localityLabels, localityCovered, localityTotal, localityPct, "x");



    var zoneLabels = [<% for (int i=0; i<zoneData.size(); i++) {%>
        "<%= zoneData.get(i).getLabel() %>"<%= i<zoneData.size()-1 ? "," : "" %>
    <%}%>];

    var zoneCovered = [<% for (int i=0; i<zoneData.size(); i++) {%>
        <%= zoneData.get(i).getStoresInCampaign() %><%= i<zoneData.size()-1 ? "," : "" %>
    <%}%>];

    var zoneTotal = [<% for (int i=0; i<zoneData.size(); i++) {%>
        <%= zoneData.get(i).getTotalStores() %><%= i<zoneData.size()-1 ? "," : "" %>
    <%}%>];

    var zonePct = [<% for (int i=0; i<zoneData.size(); i++) {%>
        <%= zoneData.get(i).getCoveragePercentage() %><%= i<zoneData.size()-1 ? "," : "" %>
    <%}%>];

    renderChart("zoneChart", zoneLabels, zoneCovered, zoneTotal, zonePct, "y");

</script>
<% } %>

</body>
</html> 

