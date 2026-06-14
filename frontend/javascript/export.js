
async function exportarExcel(resource) {
    const API_BASE = 'http://localhost:8080';
    const btn = document.querySelector('.btn-export');
    const textoOriginal = btn.textContent;

    btn.textContent = 'Generando...';
    btn.disabled = true;

    try {
        let url = `${API_BASE}/api/export/${resource}`;
        
        const token = sessionStorage.getItem('token');
        const headers = {};
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const response = await fetch(url, { headers });

        if (!response.ok) {
            alert('Error al generar el archivo.');
            return;
        }

        // Descarga automática del archivo
        const blob = await response.blob();
        const downloadUrl = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = `${resource}_export.xlsx`;
        a.click();
        URL.revokeObjectURL(downloadUrl);

    } finally {
        btn.textContent = textoOriginal;
        btn.disabled = false;
    }
}