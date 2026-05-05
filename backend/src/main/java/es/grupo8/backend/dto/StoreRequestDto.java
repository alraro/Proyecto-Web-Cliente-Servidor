package es.grupo8.backend.dto;

public class StoreRequestDto {

    private String name;
    private String address;
    private String postalCode;
    private Integer chainId;

    public StoreRequestDto() {}

    public String getName()            { return name; }
    public void setName(String name)   { this.name = name; }

    public String getAddress()                 { return address; }
    public void setAddress(String address)     { this.address = address; }

    public String getPostalCode()              { return postalCode; }
    public void setPostalCode(String pc)       { this.postalCode = pc; }

    public Integer getChainId()                { return chainId; }
    public void setChainId(Integer chainId)    { this.chainId = chainId; }
}