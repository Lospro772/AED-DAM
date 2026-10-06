package com.docencia.numeros;
/** Servicio para practicar Math. */
public interface MathService {
    Double calcularAreaCirculo(Double radio);
    Integer calcularPotencia(Integer base, Integer exponente);
    Integer redondearHaciaArriba(Double numero);
    Double calcularRaizCuadrada(Double numero);
    Integer obtenerValorAbsoluto(Integer numero);
}
