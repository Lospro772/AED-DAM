package com.docencia.algoritmos;
import java.util.List;


public interface OrdenacionService {
    List<Integer> ordenarBurbujaAscendente(List<Integer> numeros);
    List<Integer> ordenarBurbujaDescendente(List<Integer> numeros);
    List<String> ordenarPalabrasAlfabeticamente(List<String> palabras);
    List<Integer> invertirLista(List<Integer> numeros);
    Boolean estaOrdenadaAscendente(List<Integer> numeros);
}
