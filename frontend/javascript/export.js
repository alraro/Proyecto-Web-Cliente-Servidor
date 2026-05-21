async function exportarExcel(resource, campaignId = null) {
    const btn = document.querySelector('.btn-export');
    const textoOriginal = btn.textContent;

    btn.textContent = 'Generando...';
    btn.disabled = true;

    try {
        let url = `http://localhost:8080/api/export/${resource}`;
        
        /* Lo usamos solo si necesitamos exportar tambien el dashboard (en proceso)
        if (campaignId) url += `?campaignId=${campaignId}`;
        */
        const token = localStorage.getItem('token');
        const headers = {};
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const response = await fetch(url, { headers });

        if (response.status === 403) {
            alert('No tienes permisos.');
            return;
        }

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