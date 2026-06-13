const gestionCards = []

gestionCards.push({
    icon: "🏪",
    title: "Cadenas de supermercados",
    description: "Crear, editar y eliminar cadenas. Activar o desactivar su participacion en campanas.",
    link: "/admin/chains"
});

gestionCards.push({
    icon: "📋",
    title: "Campañas",
    description: "Crear, editar y eliminar campañas y asignar sus tiendas participantes.",
    link: "/admin/campaigns"
});

gestionCards.push({
    icon: "📅",
    title: "Ver campañas",
    description: "Consultar el estado de todas las campañas de recogida.",
    link: "/admin/view-campaigns"
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

gestionCards.push({
    icon: "🚨",
    title: "Incidencias",
    description: "Visualizar y gestionar las incidencias",
    link: "/admin/incidents"
})

export default gestionCards;