export const ROLE_ROUTES = {
    ADMINISTRADOR: '/admin',
    COORDINADOR: '/coordinator',
    CAPITAN: '/captain',
    COLABORADOR: '/colaborator',
    RESPONSABLE_TIENDA: '/responsible',
};

export function getRoleRoute(role) {
    return ROLE_ROUTES[role] ?? '/';
}
