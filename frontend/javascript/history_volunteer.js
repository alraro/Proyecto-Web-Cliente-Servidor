const form = document.querySelector('#history-filter-form');
const volunteerInput = document.querySelector('#volunteer-id');
const pageSizeSelect = document.querySelector('#page-size');
const prevButton = document.querySelector('#prev-page');
const nextButton = document.querySelector('#next-page');
const pageIndicator = document.querySelector('#page-indicator');
const historyMessage = document.querySelector('#history-message');
const historyMeta = document.querySelector('#history-meta');
const historyBody = document.querySelector('#history-tbody');

const API_BASE_URL = `${window.location.protocol}//${window.location.hostname}:8080`;
const AUTH_TOKEN_KEY = 'bancosol_auth_token';

let currentVolunteerId = '';
let currentPage = 1;
let currentSize = Number(pageSizeSelect.value);
let totalPages = 1;

function getStoredToken() {
    return sessionStorage.getItem(AUTH_TOKEN_KEY) || localStorage.getItem(AUTH_TOKEN_KEY);
}

function clearStoredToken() {
    sessionStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(AUTH_TOKEN_KEY);
}

function isTokenExpired(token) {
    try {
        const parts = token.split('.');
        if (parts.length !== 3) {
            return true;
        }

        const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
        if (!payload.exp) {
            return false;
        }

        return Date.now() >= payload.exp * 1000;
    } catch {
        return true;
    }
}

function setMessage(text, type) {
    historyMessage.textContent = text;
    historyMessage.classList.remove('is-error', 'is-success');
    if (type) {
        historyMessage.classList.add(type);
    }
}

function parseShiftLabel(shift) {
    if (!shift || typeof shift !== 'object') {
        return 'Turno registrado';
    }

    const date = shift.date || shift.fecha || '-';
    const start = shift.startTime || shift.horaInicio || shift.start || '-';
    const end = shift.endTime || shift.horaFin || shift.end || '-';
    const role = shift.role || shift.rol || shift.tipo || '';

    return role ? `${date} (${start} - ${end}) · ${role}` : `${date} (${start} - ${end})`;
}

function getCampaignName(entry) {
    return entry.campaignName || entry.nombreCampana || entry.campaign?.name || 'Campaña sin nombre';
}

function getCampaignStatus(entry) {
    return entry.status || entry.estado || entry.campaign?.status || 'Sin estado';
}

function getShifts(entry) {
    if (Array.isArray(entry.shifts)) {
        return entry.shifts;
    }
    if (Array.isArray(entry.turnos)) {
        return entry.turnos;
    }
    if (Array.isArray(entry.campaignShifts)) {
        return entry.campaignShifts;
    }
    return [];
}

function renderRows(items) {
    if (!items.length) {
        historyBody.innerHTML = '<tr><td colspan="3" class="empty-state">No se encontraron campañas para este voluntario.</td></tr>';
        return;
    }

    const rows = items.map((entry) => {
        const shifts = getShifts(entry);
        const shiftsHtml = shifts.length
            ? `<ul class="shift-list">${shifts.map((shift) => `<li>${parseShiftLabel(shift)}</li>`).join('')}</ul>`
            : 'Sin turnos registrados';

        return `
            <tr>
                <td>${getCampaignName(entry)}</td>
                <td>${getCampaignStatus(entry)}</td>
                <td>${shiftsHtml}</td>
            </tr>
        `;
    });

    historyBody.innerHTML = rows.join('');
}

function normalizePayload(payload) {
    const items = payload.items || payload.content || payload.data || payload.historial || [];
    const page = Number(payload.page ?? payload.number ?? payload.currentPage ?? 1);
    const pages = Number(payload.totalPages ?? payload.pages ?? 1);
    const total = Number(payload.totalItems ?? payload.totalElements ?? items.length ?? 0);

    return {
        items: Array.isArray(items) ? items : [],
        page: Number.isFinite(page) && page > 0 ? page : 1,
        totalPages: Number.isFinite(pages) && pages > 0 ? pages : 1,
        totalItems: Number.isFinite(total) && total >= 0 ? total : 0,
    };
}

function refreshPagination() {
    pageIndicator.textContent = `Página ${currentPage} de ${totalPages}`;
    prevButton.disabled = currentPage <= 1;
    nextButton.disabled = currentPage >= totalPages;
}

async function fetchHistory(pageToLoad) {
    if (!currentVolunteerId) {
        setMessage('Ingresa un ID de voluntario para consultar el historial.', 'is-error');
        return;
    }

    setMessage('Cargando historial...', null);

    const token = getStoredToken();
    if (!token || isTokenExpired(token)) {
        clearStoredToken();
        window.location.href = 'login.html?reason=session-expired';
        return;
    }

    try {
        const query = new URLSearchParams({
            page: String(pageToLoad),
            size: String(currentSize),
        });

        const response = await fetch(
            `${API_BASE_URL}/api/volunteers/${encodeURIComponent(currentVolunteerId)}/history?${query.toString()}`,
            {
                method: 'GET',
                headers: {
                    Accept: 'application/json',
                    Authorization: `Bearer ${token}`,
                },
            }
        );

        if (!response.ok) {
            const fallbackText = 'No se pudo consultar el historial del voluntario.';
            let backendMessage = '';
            try {
                const errorPayload = await response.json();
                backendMessage = errorPayload.message || errorPayload.error || '';
            } catch {
                backendMessage = '';
            }
            throw new Error(backendMessage || fallbackText);
        }

        const payload = await response.json();
        const normalized = normalizePayload(payload);

        currentPage = normalized.page;
        totalPages = normalized.totalPages;

        renderRows(normalized.items);
        refreshPagination();

        historyMeta.textContent = `Mostrando ${normalized.items.length} registros en esta página. Total disponible: ${normalized.totalItems}.`;
        setMessage('Historial cargado correctamente.', 'is-success');
    } catch (error) {
        renderRows([]);
        historyMeta.textContent = '';
        totalPages = 1;
        currentPage = 1;
        refreshPagination();
        setMessage(error.message || 'Error al conectar con el backend.', 'is-error');
    }
}

const initialToken = getStoredToken();
if (!initialToken || isTokenExpired(initialToken)) {
    clearStoredToken();
    window.location.href = 'login.html?reason=session-expired';
}

form.addEventListener('submit', (event) => {
    event.preventDefault();

    const nextVolunteerId = volunteerInput.value.trim();
    currentSize = Number(pageSizeSelect.value);

    if (!nextVolunteerId) {
        setMessage('El ID de voluntario es obligatorio.', 'is-error');
        volunteerInput.focus();
        return;
    }

    currentVolunteerId = nextVolunteerId;
    currentPage = 1;
    totalPages = 1;
    refreshPagination();

    fetchHistory(currentPage);
});

pageSizeSelect.addEventListener('change', () => {
    currentSize = Number(pageSizeSelect.value);
    if (currentVolunteerId) {
        currentPage = 1;
        fetchHistory(currentPage);
    }
});

prevButton.addEventListener('click', () => {
    if (currentPage > 1) {
        fetchHistory(currentPage - 1);
    }
});

nextButton.addEventListener('click', () => {
    if (currentPage < totalPages) {
        fetchHistory(currentPage + 1);
    }
});

refreshPagination();
