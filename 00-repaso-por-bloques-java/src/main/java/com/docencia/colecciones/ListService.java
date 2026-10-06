package com.docencia.colecciones;

import java.util.List;

/**
 * Servicio para trabajar con listas.
 */
public interface ListService {
    /**
     * Filtra palabras por longitud mínima.
     *
     * @param palabras lista de palabras
     * @param longitudMinima longitud mínima permitida
     * @return lista filtrada
     * @throws IllegalArgumentException si los datos son inválidos
     */
    List<String> filtrarPalabrasPorLongitud(List<String> palabras, Integer longitudMinima);

    /**
     * Ordena números de forma ascendente.
     *
     * @param numeros lista de números
     * @return lista ordenada
     * @throws IllegalArgumentException si la lista es null
     */
    List<Integer> ordenarNumerosAscendente(List<Integer> numeros);

    /**
     * Suma los elementos de una lista.
     *
     * @param numeros lista de números
     * @return suma total
     * @throws IllegalArgumentException si la lista es null
     */
    Integer sumarElementosLista(List<Integer> numeros);

    /**
     * Calcula la media de una lista.
     *
     * @param numeros lista de números
     * @return media aritmética
     * @throws IllegalArgumentException si la lista es null o vacía
     */
    Double calcularMediaLista(List<Integer> numeros);

    /**
     * Elimina números duplicados.
     *
     * @param numeros lista de números
     * @return lista sin duplicados
     * @throws IllegalArgumentException si la lista es null
     */
    List<Integer> eliminarNumerosDuplicados(List<Integer> numeros);
}
