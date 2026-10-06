package com.docencia.colecciones;

import java.util.List;
import java.util.Map;

/**
 * Servicio para trabajar con mapas.
 */
public interface MapService {
    Map<String, Integer> contarFrecuenciaPalabras(List<String> palabras);
    Integer obtenerValorPorClave(Map<String, Integer> mapa, String clave);
    Map<String, Double> calcularMediaPorCategoria(Map<String, List<Integer>> datos);
    String obtenerClaveConMayorValor(Map<String, Integer> mapa);
    Map<String, Integer> filtrarPorValorMinimo(Map<String, Integer> mapa, Integer minimo);
}
