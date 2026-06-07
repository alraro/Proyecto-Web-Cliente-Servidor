// import gestionCards from './CardsByRole/Admin/sections/AdminCards';
// import capitanCards from './CardsByRole/Capitan/sections/CapitanCards';
// import coordinadorCards from './CardsByRole/Coordinador/sections/CoordinadorCards';
// import colaboradorCards from './CardsByRole/Colaborador/sections/ColaboradorCards';
// import pendingCards from './CardsByRole/Admin/sections/AdminPendingCards';
// import MenuCardSection from '../MenuCards/MenuCardSection';
// import {useAuth} from '../auth/useAuthHook';

// function MenuCardsList() {
//     const { usuario } = useAuth();
//     const role = usuario?.role ?? "";

//     if(role === 'ADMINISTRADOR'){
//         return (
//             <>
//             <MenuCardSection title="Gestión" cards={gestionCards} />
//             <MenuCardSection title="Pendientes" cards={pendingCards} />
//             </>
//         );
//     }

//     if (role === 'CAPITAN') {
//         return (
//             <>
//             <MenuCardSection title="Gestión" cards={capitanCards} />
//             </>
//         )
//     }

//     if (role === 'COORDINADOR') {
//         return (
//             <>
//             <MenuCardSection title="Gestión" cards={coordinadorCards} />
//             </>
//         )
//     }

//     if (role === 'COLABORADOR') {
//         return (
//             <>
//             <MenuCardSection title="Gestión" cards={colaboradorCards} />
//             </>
//         )
//     }

//     return null;
// }

// export default MenuCardsList;