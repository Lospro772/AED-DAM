package com.docencia.condicionales;
/** Servicio para practicar if/else. */
public interface IfElseService {
    String clasificarEdad(Integer edad);
    String evaluarNota(Integer nota);
    Boolean puedeAcceder(Boolean activo, Integer edad);
    String clasificarTemperatura(Double temperatura);
    String calcularResultadoComparacion(Integer primero, Integer segundo);
}
