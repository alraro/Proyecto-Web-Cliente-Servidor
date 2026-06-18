const gestionCards = [];

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