package es.grupo8.backend.dto;

public class PartnerEntityManagerUpdateRequestDto {

    private String name;
    private String email;
    private String phone;
    private String address;
    private String postalCode;
    private Integer partnerEntityId;

    public PartnerEntityManagerUpdateRequestDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Integer getPartnerEntityId() {
        return partnerEntityId;
    }

    public void setPartnerEntityId(Integer partnerEntityId) {
        this.partnerEntityId = partnerEntityId;
    }
}
