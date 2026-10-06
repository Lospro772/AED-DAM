package com.docencia.herencia;
/** Servicio para practicar herencia. */
public interface HerenciaService {
    String obtenerNombreCompleto(Persona persona);
    Boolean esAlumno(Persona persona);
    String obtenerDescripcionPersona(Persona persona);
    Boolean tieneEdadMinima(Persona persona, Integer edadMinima);
    String obtenerTipoPersona(Persona persona);
}
