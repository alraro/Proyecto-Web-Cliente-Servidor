import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { screen, within, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from '../../test/renderWithProviders';
import { CampaignModal } from './AdminCampaings';

const jsonOk = (data) => Promise.resolve({ ok: true, json: () => Promise.resolve(data) });

const storesResponse = {
    content: [
        { id: 10, name: 'Mercadona Centro', chainName: 'Mercadona', locality: 'Madrid' },
        { id: 11, name: 'Carrefour Sur', chainName: 'Carrefour', locality: 'Madrid' },
    ],
};

function installFetchMock() {
    globalThis.fetch = vi.fn((url) => {
        if (url.includes('/api/chains')) return jsonOk([{ id: 1, name: 'Mercadona' }]);
        if (url.includes('/api/zones')) return jsonOk([{ id: 1, name: 'Centro' }]);
        if (url.includes('/api/localities')) return jsonOk([{ id: 1, name: 'Madrid' }]);
        if (url.includes('/api/stores')) return jsonOk(storesResponse);
        if (/\/api\/campaigns\/\d+\/stores/.test(url)) return jsonOk({ stores: [] });
        return jsonOk({});
    });
}

const campaignTypes = [{ id: 1, name: 'Recogida' }];

function renderModal(props = {}) {
    const onSubmit = vi.fn();
    const onClose = vi.fn();
    renderWithProviders(
        <CampaignModal
            title="Agregar campaña"
            isOpen={true}
            onClose={onClose}
            onSubmit={onSubmit}
            campaign={null}
            campaignTypes={campaignTypes}
            {...props}
        />
    );
    return { onSubmit, onClose };
}

beforeEach(() => {
    installFetchMock();
});

afterEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
});

describe('CampaignModal (panel dual de tiendas)', () => {
    it('el botón + mueve una tienda de disponibles a seleccionadas', async () => {
        renderModal();
        const storeRow = (await screen.findByText(/Mercadona Centro/)).closest('li');

        await userEvent.click(within(storeRow).getByRole('button', { name: '+' }));

        // ahora aparece el botón de quitar (×) y solo queda 1 tienda disponible
        expect(screen.getByRole('button', { name: '×' })).toBeInTheDocument();
        expect(screen.getAllByRole('button', { name: '+' })).toHaveLength(1);
        expect(screen.getByText(/Mercadona Centro/)).toBeInTheDocument();
    });

    it('"Filtrar" relanza GET /api/stores con los params del filtro', async () => {
        renderModal();
        await screen.findByText(/Mercadona Centro/);

        const chainSelect = screen.getByRole('option', { name: 'Todas las cadenas' }).closest('select');
        await userEvent.selectOptions(chainSelect, '1');
        await userEvent.click(screen.getByRole('button', { name: 'Filtrar' }));

        await waitFor(() =>
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('chainId=1'),
                expect.objectContaining({ headers: expect.any(Object) })
            )
        );
    });

    it('valida que la fecha de fin sea posterior a la de inicio', async () => {
        const { onSubmit } = renderModal();
        await screen.findByText(/Mercadona Centro/);

        await userEvent.type(screen.getByLabelText('Nombre'), 'Navidad');
        await userEvent.selectOptions(screen.getByLabelText('Tipo'), '1');
        fireEvent.change(screen.getByLabelText('Fecha de inicio'), { target: { value: '2025-12-10' } });
        fireEvent.change(screen.getByLabelText('Fecha de fin'), { target: { value: '2025-12-01' } });

        await userEvent.click(screen.getByRole('button', { name: 'Guardar' }));

        expect(onSubmit).not.toHaveBeenCalled();
        expect(screen.getByText('La fecha de fin debe ser posterior a la de inicio.')).toBeInTheDocument();
    });

    it('onSubmit recibe formData y los ids de las tiendas seleccionadas', async () => {
        const { onSubmit } = renderModal();
        const storeRow = (await screen.findByText(/Mercadona Centro/)).closest('li');

        await userEvent.type(screen.getByLabelText('Nombre'), 'Navidad');
        await userEvent.selectOptions(screen.getByLabelText('Tipo'), '1');
        fireEvent.change(screen.getByLabelText('Fecha de inicio'), { target: { value: '2025-12-01' } });
        fireEvent.change(screen.getByLabelText('Fecha de fin'), { target: { value: '2025-12-31' } });
        await userEvent.click(within(storeRow).getByRole('button', { name: '+' }));

        await userEvent.click(screen.getByRole('button', { name: 'Guardar' }));

        await waitFor(() =>
            expect(onSubmit).toHaveBeenCalledWith(
                expect.objectContaining({
                    name: 'Navidad',
                    typeId: '1',
                    startDate: '2025-12-01',
                    endDate: '2025-12-31',
                }),
                [10]
            )
        );
    });
});
