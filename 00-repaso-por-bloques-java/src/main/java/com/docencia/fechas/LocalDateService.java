package com.docencia.fechas;
import java.time.LocalDate;
/** Servicio para practicar LocalDate. */
public interface LocalDateService {
    Integer calcularEdad(LocalDate fechaNacimiento);
    Boolean esFechaFutura(LocalDate fecha);
    Long calcularDiasEntreFechas(LocalDate inicio, LocalDate fin);
    LocalDate sumarDias(LocalDate fecha, Integer dias);
    Boolean esMayorDeEdad(LocalDate fechaNacimiento);
}
