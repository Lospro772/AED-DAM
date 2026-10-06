package com.docencia.numeros;
/** Servicio para practicar Integer. */
public interface IntegerService {
    Boolean esPar(Integer numero);
    Integer sumarDigitos(Integer numero);
    Integer convertirTextoAEntero(String texto);
    Boolean esNumeroPrimo(Integer numero);
    Integer calcularFactorial(Integer numero);
}
