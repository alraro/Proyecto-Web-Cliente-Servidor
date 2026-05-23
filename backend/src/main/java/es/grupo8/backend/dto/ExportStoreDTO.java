package es.grupo8.backend.dto;

public class StoreExportDTO {
    private Integer id;
    private String name;
    private String address;
    private String locality;
    private String postalCode;
    private String zone;
    private String chain;

    public StoreExportDTO(Integer id, String name, String address, String locality, String postalCode, String zone, String chain) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.locality = locality;
        this.postalCode = postalCode;
        this.zone = zone;
        this.chain = chain;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }
}