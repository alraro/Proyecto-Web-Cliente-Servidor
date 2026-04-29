package es.grupo8.backend.dto;

public class CoverageItemDTO {
    private String label;           // nombre de cadena / localidad / zona
    private long totalStores;       // total tiendas en esa agrupación
    private long storesInCampaign;  // tiendas que participan en la campaña
    private double coveragePercent; // porcentaje calculado

    public CoverageItemDTO(String label, long totalStores, long storesInCampaign) {
        this.label = label;
        this.totalStores = totalStores;
        this.storesInCampaign = storesInCampaign;
        this.coveragePercent = totalStores > 0
            ? Math.round((storesInCampaign * 100.0 / totalStores) * 10.0) / 10.0
            : 0.0;
    }

    public String getLabel() {
        return label;
    }

    public long getTotalStores() {
        return totalStores;
    }

    public long getStoresInCampaign() {
        return storesInCampaign;
    }

    public double getCoveragePercent() {
        return coveragePercent;
    }
}
