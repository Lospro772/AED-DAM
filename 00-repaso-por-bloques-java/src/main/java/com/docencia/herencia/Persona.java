package com.docencia.herencia;

public abstract class Persona {
    private final String nombre;
    private final String apellidos;
    private final Integer edad;

    protected Persona(String nombre, String apellidos, Integer edad) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.edad = edad;
    }

    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public Integer getEdad() { return edad; }
}
