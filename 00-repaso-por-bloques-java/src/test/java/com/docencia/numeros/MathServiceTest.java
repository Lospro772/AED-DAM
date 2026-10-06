package com.docencia.numeros;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MathServiceTest {
    private final MathService service = new MathServiceImpl();

    @Test @Order(1) void calcularAreaCirculoRadioValidoDevuelveArea() { assertEquals(Math.PI * 4, service.calcularAreaCirculo(2.0)); }
    @Test @Order(2) void calcularAreaCirculoRadioNegativoLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.calcularAreaCirculo(-1.0)); }
    @Test @Order(3) void calcularPotenciaDatosValidosDevuelveResultado() { assertEquals(8, service.calcularPotencia(2, 3)); }
    @Test @Order(4) void calcularPotenciaExponenteCeroDevuelveUno() { assertEquals(1, service.calcularPotencia(5, 0)); }
    @Test @Order(5) void redondearHaciaArribaDecimalDevuelveEnteroSuperior() { assertEquals(3, service.redondearHaciaArriba(2.1)); }
    @Test @Order(6) void calcularRaizCuadradaNumeroValidoDevuelveRaiz() { assertEquals(3.0, service.calcularRaizCuadrada(9.0)); }
    @Test @Order(7) void obtenerValorAbsolutoNumeroNegativoDevuelvePositivo() { assertEquals(5, service.obtenerValorAbsoluto(-5)); }
}
