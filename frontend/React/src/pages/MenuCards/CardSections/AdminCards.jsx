const gestionCards = []

gestionCards.push({
    icon: "CH",
    title: "Cadenas de supermercados",
    description: "Crear, editar y eliminar cadenas. Activar o desactivar su participacion en campanas.",
    link: "/admin/chains"
});

gestionCards.push({
    icon: "DB",
    title: "Dashboard",
    description: "Visualiza cobertura por cadena, localidad y zona para cada campana.",
    link: "/admin/dashboard"
});

gestionCards.push({
    icon: "ST",
    title: "Tiendas",
    description: "Gestionar las tiendas participantes, asignar cadenas y codigos postales.",
    link: "/admin/stores"
});

export default gestionCards;