package com.docencia.algoritmos;
import java.util.List;


public interface BusquedaService {
    Integer buscarIndiceElemento(List<Integer> numeros, Integer valor);
    Boolean existePalabra(List<String> palabras, String palabra);
    Integer encontrarMaximo(List<Integer> numeros);
    Integer encontrarMinimo(List<Integer> numeros);
    Integer contarApariciones(List<Integer> numeros, Integer valor);
}
