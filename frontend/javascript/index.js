const menuButton = document.querySelector('#menu-button');
const mainNav = document.querySelector('#main-nav');
const panelMessage = document.querySelector('#panel-message');
const quickAccessButtons = document.querySelectorAll('.quick-access button');

menuButton.addEventListener('click', () => {
    const isOpen = mainNav.classList.toggle('is-open');
    menuButton.setAttribute('aria-expanded', String(isOpen));
});

quickAccessButtons.forEach((button) => {
    button.addEventListener('click', () => {
        const service = button.dataset.service;
        panelMessage.textContent = `Has seleccionado ${service}. Puedes enlazar esta acción con la parte de campañas que necesites.`;
    });
});