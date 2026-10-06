package com.docencia.colecciones;

import java.util.List;
import java.util.Set;

/**
 * Servicio para trabajar con conjuntos.
 */
public interface SetService {
    Set<String> obtenerElementosUnicos(List<String> elementos);
    Boolean contieneElemento(Set<String> elementos, String valor);
    Set<String> unirConjuntos(Set<String> primero, Set<String> segundo);
    Set<String> intersectarConjuntos(Set<String> primero, Set<String> segundo);
    Set<String> restarConjuntos(Set<String> primero, Set<String> segundo);
}
