import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../../test/renderWithProviders';
import AdminCampaigns from './AdminCampaings';

const jsonOk = (data) => Promise.resolve({ ok: true, json: () => Promise.resolve(data) });

const campaignsResponse = {
    content: [
        { id: 1, name: 'Navidad 2025', typeName: 'Recogida', startDate: '2025-12-01', endDate: '2025-12-31', typeId: 1, status: 'ACTIVE' },
        { id: 2, name: 'Verano 2024', typeName: 'Especial', startDate: '2024-06-01', endDate: '2024-06-30', typeId: 2, status: 'PAST' },
    ],
    summary: { totalActive: 1, totalFuture: 0, totalPast: 1 },
};

// Mock de fetch que enruta por URL/método, cubriendo lista, tipos y lo que pide el modal.
function installFetchMock() {
    globalThis.fetch = vi.fn((url, options = {}) => {
        const method = options.method || 'GET';
        if (url.includes('/api/campaign-types')) return jsonOk([{ id: 1, name: 'Recogida' }]);
        if (url.includes('/api/chains')) return jsonOk([{ id: 1, name: 'Mercadona' }]);
        if (url.includes('/api/zones')) return jsonOk([{ id: 1, name: 'Centro' }]);
        if (url.includes('/api/localities')) return jsonOk([{ id: 1, name: 'Madrid' }]);
        if (url.includes('/api/stores')) return jsonOk({ content: [] });
        if (/\/api\/campaigns\/\d+\/stores/.test(url)) return jsonOk({ stores: [] });
        if (/\/api\/campaigns\/\d+$/.test(url) && method === 'DELETE') return jsonOk({ message: 'ok' });
        if (url.includes('/api/campaigns')) return jsonOk(campaignsResponse);
        return jsonOk({});
    });
}

beforeEach(() => {
    installFetchMock();
});

afterEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
});

describe('AdminCampaigns (CRUD)', () => {
    it('pinta las campañas en la tabla', async () => {
        renderWithProviders(<AdminCampaigns />);
        expect(await screen.findByText('Navidad 2025')).toBeInTheDocument();
        expect(screen.getByText('Verano 2024')).toBeInTheDocument();
    });

    it('"Agregar Campañas" abre el modal', async () => {
        renderWithProviders(<AdminCampaigns />);
        await screen.findByText('Navidad 2025');

        await userEvent.click(screen.getByRole('button', { name: /Agregar Campañas/ }));

        expect(await screen.findByRole('heading', { name: 'Agregar campaña' })).toBeInTheDocument();
        expect(screen.getByText('Tiendas participantes')).toBeInTheDocument();
    });

    it('eliminar llama a DELETE /api/campaigns/{id} tras confirmar', async () => {
        const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
        renderWithProviders(<AdminCampaigns />);
        await screen.findByText('Navidad 2025');

        await userEvent.click(screen.getAllByRole('button', { name: 'Eliminar' })[0]);

        await waitFor(() =>
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/campaigns/1'),
                expect.objectContaining({ method: 'DELETE' })
            )
        );
        // la fila desaparece de la tabla (borrado optimista)
        await waitFor(() => expect(screen.queryByText('Navidad 2025')).not.toBeInTheDocument());
        confirmSpy.mockRestore();
    });

    it('no elimina si el usuario cancela el confirm', async () => {
        const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);
        renderWithProviders(<AdminCampaigns />);
        await screen.findByText('Navidad 2025');

        await userEvent.click(screen.getAllByRole('button', { name: 'Eliminar' })[0]);

        expect(fetch).not.toHaveBeenCalledWith(
            expect.stringContaining('/api/campaigns/1'),
            expect.objectContaining({ method: 'DELETE' })
        );
        expect(screen.getByText('Navidad 2025')).toBeInTheDocument();
        confirmSpy.mockRestore();
    });
});
