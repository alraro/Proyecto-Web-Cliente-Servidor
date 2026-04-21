package es.grupo8.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "administradores")
public class AdminEntity {

    // Esta tabla solo tiene una columna — el ID del usuario administrador.
    // No es @GeneratedValue porque el valor lo asigna la tabla Usuario,
    // aquí simplemente se referencia.
    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
}