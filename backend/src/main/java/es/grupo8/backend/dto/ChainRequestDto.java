package es.grupo8.backend.dto;

public class ChainRequestDto {

    private String name;
    private String code;
    private Boolean participation;

    public ChainRequestDto() {}

    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }

    public String getCode()                        { return code; }
    public void setCode(String code)               { this.code = code; }

    public Boolean getParticipation()              { return participation; }
    public void setParticipation(Boolean p)        { this.participation = p; }
}