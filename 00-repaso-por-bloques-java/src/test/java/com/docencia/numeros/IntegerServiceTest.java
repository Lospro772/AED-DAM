package com.docencia.numeros;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegerServiceTest {
    private final IntegerService service = new IntegerServiceImpl();

    @Test @Order(1) void esParNumeroParDevuelveTrue() { assertTrue(service.esPar(4)); }
    @Test @Order(2) void esParNumeroImparDevuelveFalse() { assertFalse(service.esPar(5)); }
    @Test @Order(3) void esParNumeroNullLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.esPar(null)); }
    @Test @Order(4) void sumarDigitosNumeroNegativoDevuelveSuma() { assertEquals(6, service.sumarDigitos(-123)); }
    @Test @Order(5) void convertirTextoAEnteroTextoValidoDevuelveEntero() { assertEquals(12, service.convertirTextoAEntero("12")); }
    @Test @Order(6) void convertirTextoAEnteroTextoInvalidoLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.convertirTextoAEntero("abc")); }
    @Test @Order(7) void esNumeroPrimoNumeroPrimoDevuelveTrue() { assertTrue(service.esNumeroPrimo(7)); }
    @Test @Order(8) void esNumeroPrimoNumeroNoPrimoDevuelveFalse() { assertFalse(service.esNumeroPrimo(8)); }
    @Test @Order(9) void calcularFactorialCeroDevuelveUno() { assertEquals(1, service.calcularFactorial(0)); }
    @Test @Order(10) void calcularFactorialNumeroNegativoLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.calcularFactorial(-1)); }
}
