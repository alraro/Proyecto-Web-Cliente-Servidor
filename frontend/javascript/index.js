const menuButton = document.querySelector('#menu-button');
const mainNav = document.querySelector('#main-nav');
const panelMessage = document.querySelector('#panel-message');
const quickAccessLinks = document.querySelectorAll('.quick-link');

if (menuButton && mainNav) {
    menuButton.addEventListener('click', () => {
        const isOpen = mainNav.classList.toggle('is-open');
        menuButton.setAttribute('aria-expanded', String(isOpen));
    });
}

quickAccessLinks.forEach((link) => {
    link.addEventListener('mouseenter', () => {
        const label = link.textContent?.trim();
        if (panelMessage && label) {
            panelMessage.textContent = `Vas a ir a: ${label}.`;
        }
    });
});