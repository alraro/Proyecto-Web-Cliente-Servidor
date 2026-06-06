const gestionCards = []

gestionCards.push({
    icon: "🏪",
    title: "Cadenas de supermercados",
    description: "Crear, editar y eliminar cadenas. Activar o desactivar su participacion en campanas.",
    link: "/admin/chains"
});

gestionCards.push({
    icon: "📊",
    title: "Dashboard",
    description: "Visualiza cobertura por cadena, localidad y zona para cada campana.",
    link: "/admin/dashboard"
});

gestionCards.push({
    icon: "🏬",
    title: "Tiendas",
    description: "Gestionar las tiendas participantes, asignar cadenas y codigos postales.",
    link: "/admin/stores"
});

gestionCards.push({
    icon: "🙋🏻‍♂️",
    title: "Voluntarios",
    description: "Gestionar voluntarios, asignar roles y responsabilidades.",
    link: "/admin/volunteers"
});

gestionCards.push({
    icon: "👤",
    title: "Creación usuarios",
    description: "Crear nuevos usuarios.",
    link: "/admin/create-user"
});

export default gestionCards;