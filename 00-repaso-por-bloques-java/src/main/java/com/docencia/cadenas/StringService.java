package com.docencia.cadenas;


public interface StringService {
    String normalizarTexto(String texto);
    Boolean esPalindromo(String texto);
    Integer contarVocales(String texto);
    String extraerIniciales(String nombreCompleto);
    String invertirTexto(String texto);
    Boolean contieneSoloLetras(String texto);
}
