package es.ejemplo.taller.orm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vehiculos")
public class VehiculoEntity { // Eliminada la clase contenedora

    @Id
    @Column(name = "matricula")
    private String matricula;

    @Column(name = "marca")
    private String marca;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "id_cliente")
    private Integer idCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", insertable = false, updatable = false)
    private ClienteEntity cliente;

    // Constructor público o protegido para JPA
    public VehiculoEntity() {
    }

    // Getters y Setters
    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public ClienteEntity getCliente() {
        return cliente;
    }

    public void setCliente(ClienteEntity cliente) {
        this.cliente = cliente;
    }
}
