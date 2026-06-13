import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../../test/renderWithProviders';
import Campaigns from './Campaigns';

const apiResponse = {
    content: [
        { id: 1, name: 'Navidad 2025', typeName: 'Recogida', startDate: '2025-12-01', endDate: '2025-12-31', status: 'ACTIVE' },
        { id: 2, name: 'Verano 2024', typeName: 'Especial', startDate: '2024-06-01', endDate: '2024-06-30', status: 'PAST' },
    ],
    summary: { totalActive: 1, totalFuture: 0, totalPast: 1 },
};

beforeEach(() => {
    globalThis.fetch = vi.fn(() =>
        Promise.resolve({ ok: true, json: () => Promise.resolve(apiResponse) })
    );
});

afterEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
});

describe('Campaigns (ver campañas)', () => {
    it('pinta las campañas que devuelve el backend', async () => {
        renderWithProviders(<Campaigns />);
        expect(await screen.findByText('Navidad 2025')).toBeInTheDocument();
        expect(screen.getByText('Verano 2024')).toBeInTheDocument();
        // se ha llamado al endpoint de campañas
        expect(fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/campaigns?size=50'),
            expect.objectContaining({ headers: expect.any(Object) })
        );
    });

    it('los chips muestran los contadores del summary', async () => {
        renderWithProviders(<Campaigns />);
        expect(await screen.findByRole('button', { name: /Activas: 1/ })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Pasadas: 1/ })).toBeInTheDocument();
    });

    it('el chip de estado filtra las filas', async () => {
        renderWithProviders(<Campaigns />);
        await screen.findByText('Navidad 2025');

        await userEvent.click(screen.getByRole('button', { name: /Pasadas/ }));

        await waitFor(() => expect(screen.queryByText('Navidad 2025')).not.toBeInTheDocument());
        expect(screen.getByText('Verano 2024')).toBeInTheDocument();
    });

    it('el buscador filtra por nombre', async () => {
        renderWithProviders(<Campaigns />);
        await screen.findByText('Navidad 2025');

        await userEvent.type(screen.getByPlaceholderText('Buscar...'), 'verano');

        await waitFor(() => expect(screen.queryByText('Navidad 2025')).not.toBeInTheDocument());
        expect(screen.getByText('Verano 2024')).toBeInTheDocument();
    });
});
