const gestionCards = [];

gestionCards.push({
    icon: "📋",
    title: "Mis campañas",
    description: "Consulta las campañas y recursos que coordinas desde tu panel principal.",
    link: "/coordinator"
});

gestionCards.push({
    icon: "🏬",
    title: "Mis tiendas",
    description: "Revisa las tiendas asignadas a tu área de trabajo.",
    link: "/coordinator"
});

gestionCards.push({
    icon: "⏰",
    title: "Crear turno",
    description: "Crea un nuevo turno de recogida para una campaña y tienda.",
    link: "/coordinator/create-shift"
});

gestionCards.push({
    icon: "📅",
    title: "Calendario de turnos",
    description: "Visualiza todos los turnos de una campaña agrupados por tienda y día.",
    link: "/coordinator/shifts-calendar"
});

export default gestionCards;