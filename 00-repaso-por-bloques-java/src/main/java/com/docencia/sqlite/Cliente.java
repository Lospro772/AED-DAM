package com.docencia.sqlite;

public class Cliente {
    private final String dni;
    private final String nombre;
    private final String email;
    private final String ciudad;

    public Cliente(String dni, String nombre, String email, String ciudad) {
        this.dni = dni;
        this.nombre = nombre;
        this.email = email;
        this.ciudad = ciudad;
    }

    public String getDni() { return dni; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getCiudad() { return ciudad; }
}
