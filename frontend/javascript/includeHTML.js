// DefiniciÃ³n del Web Component <include-html> 
// usando la API estÃ¡ndar de Custom Elements
class IncludeHTML extends HTMLElement {
    // connectedCallback() se ejecuta automÃ¡ticamente 
    // cuando el elemento entra en el DOM.
    async connectedCallback() {
        // Se comprueba que se ha pasado el atributo 'src' 
        // que indica la URL del archivo HTML a importar.
        const src = this.getAttribute('src');
        if (src) {
            try {
                // Se utiliza fetch para obtener el contenido del archivo HTML.
                const response = await fetch(src);
                if (!response.ok) throw new Error(`HTTP ${response.status}`);
                this.innerHTML = await response.text();
            } catch (error) {
                console.error('Error al importar el archivo HTML:', error);
            }
        }
    }
}

// Se registra el Web Component con el nombre 'include-html'
// para que pueda ser utilizado en el HTML.
customElements.define('include-html', IncludeHTML);